package utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import org.hibernate.Session;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;

import exceptions.InvalidClassException;
import redis.clients.jedis.Transaction;
import tukano.api.Result;
import tukano.api.Result.ErrorCode;
import tukano.api.User;
import tukano.api.Short;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;

public class DBHibernate implements DB {

	private static final Logger Log = Logger.getLogger(DBHibernate.class.getName());

	private static DBHibernate instance;

	public static synchronized DBHibernate getInstance() {
		if( instance != null)
			return instance;
		instance = new DBHibernate();
		return instance;
		
	}

	public DBHibernate() {}

	public <T> List<T> sql(String query, Class<T> clazz) {
		return Hibernate.getInstance().sql(query, clazz);
	}

	public <T, U> List<U> sql(String query, Class<T> containerClazz, Class<U> expectedClazz) {
		return Hibernate.getInstance().sql(query, expectedClazz);
	}

	public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
		return Hibernate.getInstance().sql(String.format(fmt, args), clazz);
	}
	
	
	public <T> Result<T> getOne(String id, String partition, Class<T> clazz) {
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var cacheId = getCacheId(id, clazz);
			var obj = jedis.get(cacheId);

			if (obj != null && !obj.equals("null")) {
				var object = JSON.decode(obj, clazz);
				return Result.ok(object);
			}

			var res = Hibernate.getInstance().getOne(id, clazz);

			if (res.isOK()) {
				var value = JSON.encode( res.value() );
				jedis.set(cacheId, value);
			}
			return res;
		} catch( Exception x ) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}
	
	public <T> Result<T> deleteOne(T obj) {
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var id = GetId.getId(obj);
			var clazz = obj.getClass();
			var cacheId = getCacheId(id, clazz);
			jedis.del(cacheId);

			return Hibernate.getInstance().deleteOne(obj);
		} catch( Exception x ) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}

	public <T> List<Result<Void>> deleteCollection(List<T> targets) {
		try {
			if (targets.isEmpty()) {
				Log.info("DBHibernate deleteCollection received an empty list");
				return List.of();
			}

		deleteCollectionFromCache(targets);

		return Hibernate.getInstance().deleteCollection(targets);
		} catch( Exception x ) {
			x.printStackTrace();
			return List.of(Result.error(ErrorCode.INTERNAL_ERROR));
		}
	}

	public <T> Result<T> updateOne(T obj) {
		//Removing from cache first to guarantee that the cache will not have an outdated value
		//(case where the update is successful on hibernate storage but not on the cache)
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var id = GetId.getId(obj);
			var clazz = obj.getClass();
			var cacheId = getCacheId(id, clazz);
			jedis.del(cacheId);

			var res = Hibernate.getInstance().updateOne(obj);

			if (res.isOK()) {
				try {
					if (clazz == User.class || clazz == Short.class) {
						var value = JSON.encode( obj );
						jedis.set(cacheId, value);
					}
				} catch (Exception e) {
					// Update unsuccessful on cache, but successful on hibernate storage
					e.printStackTrace();
					return res;
				}
			}
			return res;
		} catch( Exception x ) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}
	
	public <T> Result<T> insertOne( T obj) {
		System.err.println("DB.insert:" + obj );
		// try (var jedis = RedisCache.getCachePool().getResource()) {
		try {
		// 	var id = GetId.getId(obj);
		// 	var clazz = obj.getClass();
		// 	if (clazz == User.class || clazz == Short.class) {
		// 		var cacheId = getCacheId(id, clazz);
		// 		if (jedis.exists(cacheId)) {
		// 			return Result.error( ErrorCode.CONFLICT );
		// 		}
		// 	}
			var res = Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
			// if (res.isOK()) {
			// 	try {
			// 		if (clazz == User.class || clazz == Short.class ) {
			// 			var cacheId = getCacheId(id, clazz);
			// 			var value = JSON.encode( obj );
			// 			jedis.set(cacheId, value);
			// 		}
			// 	} catch( Exception x ) {
			// 		x.printStackTrace();
			// 		return res;
			// 	}
			// }
			return res;
		} catch( Exception x ) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}
	
	public <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute( c::accept );
	}
	
	public <T> Result<T> transaction( Function<Session, Result<T>> func) {
		return Hibernate.getInstance().execute( func );
	}

	
	private <T> String getCacheId(String id, Class<T> clazz) {
		try {
			if (clazz.equals(User.class)) {
				return "user:" + id;
			} else if (clazz.equals(Short.class)) {
				return "short:" + id;
			} else if (clazz.equals(Following.class)) {
				return "following:" + id;
			} else if (clazz.equals(Likes.class)) {
				return "like:" + id;
			}
			throw new InvalidClassException("Invalid Class: " + clazz.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private <T> void deleteCollectionFromCache(List<T> targets) {
		try (var jedis = RedisCache.getCachePool().getResource()) {
			Transaction cacheTransaction = jedis.multi();
				for (T item : targets) {
					var clazz = item.getClass();
					if (clazz == User.class || clazz == Short.class) {
						var id = GetId.getId(item);
						var cacheId = getCacheId(id, clazz);
						cacheTransaction.del(cacheId);
					}
				}
			cacheTransaction.exec();
		} catch( Exception x ) {
			x.printStackTrace();
			throw x;
		}
	}

}

