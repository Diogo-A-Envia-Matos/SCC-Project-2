package utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import exceptions.InvalidClassException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import tukano.api.Blobs;
import tukano.api.Short;
import tukano.api.User;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;
import utils.Props;

//TODO: Finish this file
//TODO: Implement interface
public class RedisCache {
    
    // Have to make the calls to DBCosmos search this cache first before checking the database

    // TODO: Choose wether to use write-through or write-back

    private static final String RedisHostname = Props.get("REDIS_URL", "");
	private static final String RedisKey = Props.get("REDIS_KEY", "");
	private static final int REDIS_PORT = 6380;
	private static final int REDIS_TIMEOUT = 1000;
	private static final boolean Redis_USE_TLS = true;
	
	private static JedisPool instance;
	
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
		instance = new JedisPool(poolConfig, RedisHostname, REDIS_PORT, REDIS_TIMEOUT, RedisKey, Redis_USE_TLS);
		return instance;
	}

	
	public synchronized static void putSession(Session s) {
		try (var jedis = getCachePool().getResource()) {
			jedis.set(s.uid(), s.user());
		}
	}
	
	public synchronized static Session getSession(String uid) {
		return sessions.get(uid);
	}

}
