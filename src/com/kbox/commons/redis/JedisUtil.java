package com.kbox.commons.redis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class JedisUtil {

	private static MyJedis jedis;
	private static int defaultExpired;
	
	private static String KEY_PREFIX = "";
	
	static {
		ResourceBundle bundle = ResourceBundle.getBundle("redis");  
	    if (bundle == null) {  
	        throw new IllegalArgumentException(  
	                "[redis.properties] is not found!");  
	    }  
	    JedisPoolConfig config = new JedisPoolConfig();  
	    config.setMaxTotal(Integer.valueOf(bundle  
	            .getString("redis.pool.maxActive")));  
	    config.setMaxIdle(Integer.valueOf(bundle  
	            .getString("redis.pool.maxIdle")));  
	    config.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWait")));  
	    config.setTestOnBorrow(Boolean.valueOf(bundle  
	            .getString("redis.pool.testOnBorrow")));  
	    config.setTestOnReturn(Boolean.valueOf(bundle  
	            .getString("redis.pool.testOnReturn")));
	    defaultExpired = Integer.valueOf(bundle.getString("redis.expired"));

	    KEY_PREFIX = bundle.getString("redis.key_prefix");
	    		
	    String[] hosts = bundle.getString("redis.hosts").split(",");
	    String[] ips = new String[hosts.length];
	    Integer[] ports = new Integer[hosts.length];
	    for (int n = 0; n < hosts.length; n++) {
	    	String[] ss = hosts[n].split(":");
	    	if (ss.length >= 1 && ss[0].trim().length() > 0) {
	    		ips[n] = ss[0].trim();
	    		if (ss.length > 1 && ss[1].trim().length() > 0) {
		    		try {
		    			ports[n] = Integer.parseInt(ss[1].trim());
		    		}
		    		catch (Exception e) {
		    			e.printStackTrace();
		    			ports[n] = 6379;
		    		}
	    		}
	    		else {
	    			ports[n] = 6379;
	    		}
	    	}
	    }
	    if (hosts.length > 1) {
	    	jedis = new MySharedJedisProxy(config, ips, ports);
	    }
	    else {
	    	jedis = new MyJedisProxy(config, ips[0], ports[0]);
	    }
	}
	
	private static class MyJedisProxy implements MyJedis {
		private JedisPool pool;
		
		public MyJedisProxy(JedisPoolConfig config, 
				String ip, Integer port) {
			pool = new JedisPool(config, ip, Integer.valueOf(port));
		}

		public void set(byte[] key, byte[] value) {
			Jedis jedis = pool.getResource();
			jedis.set(key, value);
			// jedis.expire(key, defaultExpired);
			pool.returnResource(jedis);
		}

		public void set(byte[] key, byte[] value, int expired) {
			Jedis jedis = pool.getResource();
			jedis.set(key, value);
			jedis.expire(key, expired);
			pool.returnResource(jedis);
		}
		
		public byte[] get(byte[] key) {
			Jedis jedis = pool.getResource();
			byte[] value = jedis.get(key);
			pool.returnResource(jedis);
			return value;
		}
		
		public void set(String key, Object serializeObj) {
			Jedis jedis = pool.getResource();
			jedis.set(key.getBytes(), SerializeUtil.serialize(serializeObj));
			// jedis.expire(key, defaultExpired);
			pool.returnResource(jedis);
		}

		public void set(String key, Object serializeObj, int expired) {
			Jedis jedis = pool.getResource();
			jedis.set(key.getBytes(), SerializeUtil.serialize(serializeObj));
			jedis.expire(key, expired);
			pool.returnResource(jedis);
		}
		
		public Object get(String key) {
			Jedis jedis = pool.getResource();
			Object obj = SerializeUtil.unserialize(jedis.get(key.getBytes()));
			pool.returnResource(jedis);
			return obj;
		}
	}
	
	private static class MySharedJedisProxy implements MyJedis {
		private ShardedJedisPool shardedPool;
		
		public MySharedJedisProxy(JedisPoolConfig config,
				String[] ips, Integer[] ports) {
			List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		    for (int i = 0; i < ips.length; i++) {
//		    	String ip = ips[i].trim();
//		    	int port = 6379;
//		    	if (ports.length > i) {
//		    		port = Integer.valueOf(ports[i].trim());
//		    	}
		    	JedisShardInfo info = new JedisShardInfo(ips[i].trim(), ports[i]);
		    	shards.add(info);
		    }
		    shardedPool = new ShardedJedisPool(config, shards);
		}
		
		public void set(byte[] key, byte[] value) {
			ShardedJedis jedis = shardedPool.getResource();
			jedis.set(key, value);
			// jedis.expire(key, defaultExpired);
			shardedPool.returnResource(jedis);
		}

		public void set(byte[] key, byte[] value, int expired) {
			ShardedJedis jedis = shardedPool.getResource();
			jedis.set(key, value);
			jedis.expire(key, expired);
			shardedPool.returnResource(jedis);
		}
		
		public byte[] get(byte[] key) {
			ShardedJedis jedis = shardedPool.getResource();
			byte[] value = jedis.get(key);
			shardedPool.returnResource(jedis);
			return value;
		}
		
		public void set(String key, Object serializeObj) {
			ShardedJedis jedis = shardedPool.getResource();
			jedis.set(key.getBytes(), SerializeUtil.serialize(serializeObj));
			// jedis.expire(key, defaultExpired);
			shardedPool.returnResource(jedis);
		}

		public void set(String key, Object serializeObj, int expired) {
			ShardedJedis jedis = shardedPool.getResource();
			jedis.set(key.getBytes(), SerializeUtil.serialize(serializeObj));
			jedis.expire(key, expired);
			shardedPool.returnResource(jedis);
		}
		
		public Object get(String key) {
			ShardedJedis jedis = shardedPool.getResource();
			Object obj = SerializeUtil.unserialize(jedis.get(key.getBytes()));
			shardedPool.returnResource(jedis);
			return obj;
		}
	}
	
	private interface MyJedis {
		public Object get(String key);
		public byte[] get(byte[] key);
		
		public void set(byte[] key, byte[] value);
		public void set(byte[] key, byte[] value, int expired);
		
		public void set(String key, Object serializeObj);
		public void set(String key, Object serializeObj, int expired);
		// public void expire(String key, int expired);
	}
	
//	public static void set(String key, String value) {
//		Jedis jedis = pool.getResource();
//		jedis.set(key, value);
//		pool.returnResource(jedis);
//	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void set(byte[] key, byte[] value) {
		jedis.set(key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expired  seconds
	 */
	public static void set(byte[] key, byte[] value, int expired) {
		jedis.set(key, value, expired);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static byte[] get(byte[] key) {
		return jedis.get(key);
	}
	
	/**
	 * 
	 * @param key
	 * @param serializeObj
	 */
	public static void set(String key, Object serializeObj) {
		jedis.set(KEY_PREFIX + "." + key, serializeObj);
	}

	/**
	 * 
	 * @param key
	 * @param serializeObj
	 * @param expired   seconds
	 */
	public static void set(String key, Object serializeObj, int expired) {
		jedis.set(KEY_PREFIX + "." + key, serializeObj, expired);
	}
	
	public static Object get(String key) {
		return jedis.get(KEY_PREFIX + "." + key);
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		System.out.println("set");
		set("test_key", "test_date: " + new Date().toLocaleString());
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("get");
				System.out.println(get("test_key"));
			}
		}).start();
	}
}
