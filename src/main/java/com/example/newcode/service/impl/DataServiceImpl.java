package com.example.newcode.service.impl;

import com.example.newcode.service.DataService;
import com.example.newcode.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
	@Autowired
	RedisUtils redisUtils;

	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

	/**
	 * 将指定IP加入对应的单日UV中
	 *
	 * @param ip
	 */
	@Override
	public void recordUV(String ip) {
		String uvKey = RedisUtils.getUVKey(df.format(new Date()));
		redisUtils.hllAdd(uvKey, ip);
	}

	/**
	 * 统计指定日期范围内的区间UV
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public long calculateUV(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}

		List<String> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		// 只要calender的时间是在end之前
		while (!calendar.getTime().after(end)) {
			// 得到当天的对应的key
			String key = RedisUtils.getUVKey(df.format(calendar.getTime()));
			keyList.add(key);
			calendar.add(Calendar.DATE, 1); // 以天数为单位加一
		}

		// 合并并统计这些数据，整理该日期范围内的key，格式为uv:yyyyMMdd(start):yyyyMMdd(end)
		String redisKey = RedisUtils.getUVKey(df.format(start), df.format(end));
		redisUtils.hllUnion(redisKey, keyList.toArray(new String[0]));
		return redisUtils.hllSize(redisKey);
	}

	/**
	 * 将指定用户计入单日DAU
	 *
	 * @param userId
	 */
	@Override
	public void recordDAU(int userId) {
		String uvKey = RedisUtils.getDAUKey(df.format(new Date()));
		redisUtils.bitSet(uvKey, userId, true);
	}

	/**
	 * 统计指定日期范围内的区间DAU
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public long calculateDAU(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}
		List<byte[]> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		while (!calendar.getTime().after(end)) {
			String key = redisUtils.getDAUKey(df.format(calendar.getTime()));
			keyList.add(key.getBytes());
			calendar.add(Calendar.DATE, 1); // 以天数为单位加一
		}
		byte[][] toArray = keyList.toArray(new byte[0][0]); // 将集合返回数据的新方法
		// 合并并统计这些数据，整理该日期范围内的key，dau:yyyyMMdd(start):yyyyMMdd(end)
		String redisKey = RedisUtils.getDAUKey(df.format(start), df.format(end));
		return redisUtils.bitOr(redisKey, toArray);
	}
}
