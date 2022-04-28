package com.example.newcode.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

	@Autowired
	@Qualifier("newCodeRedisTemplateConfig")
	private RedisTemplate<String, Object> redisTemplate;

	private static final String SPLIT = ":";
	private static final String PREFIX_ENTITY_LIKE = "like:entity";
	private static final String PREFIX_USER_LIKE = "like:user";
	private static final String PREFIX_FOLLOWEE = "followee";
	private static final String PREFIX_FOLLOWER = "follower";
	private static final String PREFIX_KAPTCHA = "kaptcha";
	private static final String PREFIX_TICKET = "ticket";
	private static final String PREFIX_USER = "user";
	private static final String PREFIX_UV = "uv";
	private static final String PREFIX_DAU = "dau";

	/**
	 * 某个被点赞实体的setName 即 like:entity:entityType:entityId -> set(userId)
	 *
	 * @param entityType 点赞目标对应类型（1表示目标是帖子，2表示目标是comment）
	 * @param entityId   这一类目标类型的id
	 * @return
	 */
	public static String getEntityLikeKey(int entityType, int entityId) {
		return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
	}

	/**
	 * 某个被点赞用户的key：like:user:userId -> int （包含上面的被点赞的实体）
	 *
	 * @param userId
	 * @return
	 */
	public static String getUserLikeKey(int userId) {
		return PREFIX_USER_LIKE + SPLIT + userId;
	}

	/**
	 * 某一用户关注的内容对应的k1格式：followee:userId:entityType -> zset(entityId,now)
	 *
	 * @param userId
	 * @param entityType
	 * @return
	 */
	public static String getFolloweeKey(int userId, int entityType) {
		return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
	}

	/**
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public static String getFollowerKey(int entityType, int entityId) {
		return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
	}

	// 登录验证码
	// 格式：kaptcha:kaptchaOwner -> text
	public static String getKaptchaKey(String owner) {
		return PREFIX_KAPTCHA + SPLIT + owner;
	}

	// 登录的凭证
	// 格式：ticket:ticketUUid -> LoginTicket的Json对象
	public static String getTicketKey(String ticket) {
		return PREFIX_TICKET + SPLIT + ticket;
	}

	// user对应的key；格式为：user:userId
	public static String getUserKey(int userId) {
		return PREFIX_USER + SPLIT + userId;
	}

	// 单日UV；格式为：uv:yyyyMMdd
	public static String getUVKey(String date) {
		return PREFIX_UV + SPLIT + date;
	}

	// 区间UV；格式为：uv:yyyyMMdd(start):yyyyMMdd(end)
	public static String getUVKey(String startDate, String endDate) {
		return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
	}

	// 单日活跃用户 格式为：dau:yyyyMMdd
	public static String getDAUKey(String date) {
		return PREFIX_DAU + SPLIT + date;
	}

	// 区间活跃用户 格式为：dau:yyyyMMdd(start):yyyyMMdd(end)
	public static String getDAUKey(String startDate, String endDate) {
		return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
	}

	// =============================String====================================

	/**
	 * 指定缓存失效时间
	 *
	 * @param key
	 * @param time
	 * @return
	 */
	public boolean expire(String key, long time) {
		try {
			if (time > 0) {
				redisTemplate.expire(key, time, TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 根据key 获取过期时间
	 *
	 * @param key
	 * @return
	 */
	public long getExpire(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	/**
	 * 判断key是否存在
	 *
	 * @param key
	 * @return
	 */
	public boolean hasKey(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除key值
	 *
	 * @param key
	 */
	public void del(String... key) {
		if (key != null && key.length > 0) {
			if (key.length == 1) {
				redisTemplate.delete(key[0]);
			} else {
				redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
			}
		}
	}

	/**
	 * 添加key-value
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 添加key-value并设置过期时间
	 *
	 * @param key
	 * @param value
	 * @param time
	 * @param timeUnit
	 * @return
	 */
	public boolean setWithExpire(String key, Object value, long time, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForValue().set(key, value, time, timeUnit);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获得某一用户被点赞数量
	 *
	 * @param userLikeKey
	 * @return
	 */
	public Object get(String userLikeKey) {
		try {
			return redisTemplate.opsForValue().get(userLikeKey);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean decrement(String userLikeKey) {
		try {
			redisTemplate.opsForValue().decrement(userLikeKey);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean increment(String userLikeKey) {
		try {
			redisTemplate.opsForValue().increment(userLikeKey);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =============================Hash====================================

	/**
	 * 存储map
	 *
	 * @param key
	 * @param map
	 * @return
	 */
	public boolean hmSet(String key, Map<String, Object> map) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 *
	 * @param key
	 * @param item
	 * @param value
	 * @return
	 */
	public boolean hSet(String key, String item, Object value) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * HashGet
	 *
	 * @param key  不为空
	 * @param item 不为空
	 * @return
	 */
	public Object hGet(String key, String item) {
		return redisTemplate.opsForHash().get(key, item);
	}

	/**
	 * 获取hashKey对应的所有键值
	 *
	 * @param key
	 * @return
	 */
	public Map<Object, Object> hmGet(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	public boolean hdel(String key, Object... item) {
		try {
			redisTemplate.opsForHash().delete(key, item);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =============================list====================================
	public List<Object> lGet(String key, long start, long end) {
		try {
			return redisTemplate.opsForList().range(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public long lGetListSize(String key) {
		try {
			return redisTemplate.opsForList().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Object lGetIndex(String key, long index) {
		try {
			return redisTemplate.opsForList().index(key, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean lSet(String key, Object value) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean lSet(String key, List<Object> value) {
		try {
			redisTemplate.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =============================Set====================================
	public Set<Object> sGet(String key) {
		try {
			return redisTemplate.opsForSet().members(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public long sSet(String key, Object... values) {
		try {
			Long aLong = redisTemplate.opsForSet().add(key, values);
			return aLong == null ? 0 : aLong;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public long sGetSetSize(String key) {
		try {
			return redisTemplate.opsForSet().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public long setRemove(String key, Object... values) {
		try {
			Long remove = redisTemplate.opsForSet().remove(key, values);
			return remove == null ? 0 : remove;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean sIsMember(String entityLikeKey, int UserID) {
		try {
			Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, UserID);
			return isMember;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// =============================Zset====================================
	public boolean zAdd(String key, int id, long time) {
		try {
			Boolean add = redisTemplate.opsForZSet().add(key, id, time);
			return add == null ? true : add;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Long zRemove(String key, int id) {
		try {
			Long remove = redisTemplate.opsForZSet().remove(key, id);
			return remove == null ? 0l : remove;
		} catch (Exception e) {
			e.printStackTrace();
			return 0l;
		}
	}

	public Long zCard(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	/**
	 * 查询Zset中某一个score对应的value
	 *
	 * @param key
	 * @param score
	 * @return
	 */
	public Object score(String key, int score) {
		return redisTemplate.opsForZSet().score(key, score);
	}

	/**
	 * 分页查询score
	 *
	 * @param key
	 * @param offset
	 * @param limit
	 * @return
	 */
	public Set<Object> zSetRange(String key, int offset, int limit) {
		Set<Object> set = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);
		return set;
	}

	// =============================HyperLogLog====================================

	/**
	 * 向HyperLogLog数据结构对应的key中添加数据
	 *
	 * @param key
	 * @param i
	 */
	public void hllAdd(String key, String i) {
		redisTemplate.opsForHyperLogLog().add(key, i);
	}

	/**
	 * HyperLogLog数据结构对应key值的数量
	 *
	 * @param key
	 * @return
	 */
	public Long hllSize(String key) {
		Long size = redisTemplate.opsForHyperLogLog().size(key);
		return size;
	}

	/**
	 * 求多个hll数据结构的并集，就像set一样
	 *
	 * @param unionKey
	 * @param redisKey
	 */
	public void hllUnion(String unionKey, String... redisKey) {
		redisTemplate.opsForHyperLogLog().union(unionKey, redisKey);
	}

	// =============================Bitmap====================================

	/**
	 * 在Bitmap数据结构对应的key中存储对应index值（没存的地方默认为false）
	 *
	 * @param redisKey
	 * @param index
	 * @param flag
	 */
	public void bitSet(String redisKey, int index, boolean flag) {
		redisTemplate.opsForValue().setBit(redisKey, index, flag);
	}

	public boolean bitGet(String redisKey, int index) {
		return redisTemplate.opsForValue().getBit(redisKey, index);
	}

	/**
	 * 统计Bitmap数据结构对应的key中为true的值
	 *
	 * @param redisKey
	 * @return
	 */
	public Long bitCount(String redisKey) {
		// 统计
		Long count = (Long) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.bitCount(redisKey.getBytes());
			}
		});
		return count;
	}

	/**
	 * 对两个Bitmap数据结构求并集，并返回求交集后的bitCount
	 *
	 * @param resKey
	 * @param redisKey 注意是二维数组
	 * @return
	 */
	public Long bitOr(String resKey, byte[]... redisKey) {
		Long count = (Long) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.bitOp(RedisStringCommands.BitOperation.OR, resKey.getBytes(), redisKey);
				return connection.bitCount(resKey.getBytes());
			}
		});
		return count;
	}

}
