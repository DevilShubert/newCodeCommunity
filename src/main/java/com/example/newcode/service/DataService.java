package com.example.newcode.service;

import java.util.Date;

public interface DataService {
	/**
	 * 将指定IP加入对应的单日UV中
	 *
	 * @param ip
	 */
	void recordUV(String ip);

	/**
	 * 统计指定日期范围内的区间UV
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	long calculateUV(Date start, Date end);

	/**
	 * 将指定用户计入单日DAU
	 *
	 * @param userId
	 */
	void recordDAU(int userId);

	/**
	 * 统计指定日期范围内的区间DAU
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	long calculateDAU(Date start, Date end);
}
