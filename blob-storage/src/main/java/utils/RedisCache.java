package utils;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.databind.ObjectMapper;

import exceptions.InvalidClassException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.Result.ErrorCode;
import tukano.api.Short;
import tukano.api.User;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;
import utils.Props;

public class RedisCache {
    
    private static final String RedisHostname = Props.get("REDIS_URL", "redis-service");
	private static final int REDIS_PORT = 6379;
	private static final int REDIS_TIMEOUT = 1000;
	private static final boolean Redis_USE_TLS = false;
	
	private static JedisPool instance;

	private static RedisCache redisInstance;
	synchronized public static RedisCache getInstance() {
		if(redisInstance == null )
			redisInstance = new RedisCache();
		return redisInstance;
	}
	
	public synchronized static JedisPool getCachePool() {
		if( instance != null)
			return instance;
		
		var poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(poolConfig, RedisHostname, REDIS_PORT, REDIS_TIMEOUT, Redis_USE_TLS);
		return instance;
	}

	
	public void putSession(Session s) {
		try (var jedis = getCachePool().getResource()) {
			jedis.set(s.uid(), s.password());
		} catch( CosmosException ce ) {
			ce.printStackTrace();
			throw ce;
		} catch( Exception x ) {
			x.printStackTrace();
			throw x;
		}
	}
	
	public Session getSession(String uid) {
		try (var jedis = getCachePool().getResource()) {
			return new Session(uid, jedis.get(uid));
		} catch( Exception x ) {
			x.printStackTrace();
			throw x;
		}
	}

}
