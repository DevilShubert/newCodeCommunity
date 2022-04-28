package com.example.newcode.dao;

import com.example.newcode.util.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedisTemplateTest {

	@Autowired
	RedisUtils redisUtils;

	@Test
	public void testStrings() {
		String redisKey = "test:count";
		redisUtils.set(redisKey, 1);
	}

	// 统计20万个重复数据的独立总数.
	@Test
	public void testHyperLogLog() {
		String redisKey = "test:hll:01";
		for (int i = 1; i <= 100000; i++) {
			redisUtils.hllAdd(redisKey, String.valueOf(i));
		}

		for (int i = 1; i <= 100000; i++) {
			int r = (int) (Math.random() * 100000 + 1);
			redisUtils.hllAdd(redisKey, String.valueOf(r));
		}

		System.out.println(redisUtils.hllSize(redisKey));
		// 测试成功 结果为99562
	}

	// 将3组数据合并, 再统计合并后的重复数据的独立总数.
	@Test
	public void testHyperLogLogUnion() {
		String redisKey2 = "test:hll:02";
		for (int i = 1; i <= 10000; i++) {
			// 1~1000
			redisUtils.hllAdd(redisKey2, String.valueOf(i));
		}

		String redisKey3 = "test:hll:03";
		for (int i = 5001; i <= 15000; i++) {
			// 5001~15000
			redisUtils.hllAdd(redisKey3, String.valueOf(i));
		}

		String redisKey4 = "test:hll:04";
		for (int i = 10001; i <= 20000; i++) {
			// 10001~20000
			redisUtils.hllAdd(redisKey4, String.valueOf(i));
		}

		String unionKey = "test:hll:union";
		// 三个hll的并集一共应该有20000，所以在这个过程中自动去重
		redisUtils.hllUnion(unionKey, redisKey2, redisKey3, redisKey4);

		long size = redisUtils.hllSize(unionKey);
		System.out.println(size);
	}

	// 统计一组数据的布尔值
	@Test
	public void testBitMap() {
		String redisKey = "test:bm:01";

		// 记录
		redisUtils.bitSet(redisKey, 1, true);
		redisUtils.bitSet(redisKey, 4, true);
		redisUtils.bitSet(redisKey, 7, true);

		// 查询
		System.out.println(redisUtils.bitGet(redisKey, 0));// F
		System.out.println(redisUtils.bitGet(redisKey, 1));// T
		System.out.println(redisUtils.bitGet(redisKey, 2));// F

		// 统计
		System.out.println(redisUtils.bitCount(redisKey)); // 3
	}

	// 统计3组数据的布尔值, 并对这3组数据做OR运算.
	@Test
	public void testBitMapOperation() {
		String redisKey2 = "test:bm:02";
		redisUtils.bitSet(redisKey2, 0, true);
		redisUtils.bitSet(redisKey2, 1, true);
		redisUtils.bitSet(redisKey2, 2, true);

		String redisKey3 = "test:bm:03";
		redisUtils.bitSet(redisKey3, 2, true);
		redisUtils.bitSet(redisKey3, 3, true);
		redisUtils.bitSet(redisKey3, 4, true);

		String redisKey4 = "test:bm:04";
		redisUtils.bitSet(redisKey4, 4, true);
		redisUtils.bitSet(redisKey4, 5, true);
		redisUtils.bitSet(redisKey4, 6, true);

		String redisOrKey1 = "test:bm:or1";
		List<byte[]> keyList = new ArrayList<>();
		keyList.add(redisKey2.getBytes());
		keyList.add(redisKey3.getBytes());
		keyList.add(redisKey4.getBytes());
		byte[][] toArray = keyList.toArray(new byte[0][0]);
		redisUtils.bitOr(redisOrKey1, toArray);

		System.out.println(redisUtils.bitGet(redisOrKey1, 0));
		System.out.println(redisUtils.bitGet(redisOrKey1, 1));
		System.out.println(redisUtils.bitGet(redisOrKey1, 2));
		System.out.println(redisUtils.bitGet(redisOrKey1, 3));
		System.out.println(redisUtils.bitGet(redisOrKey1, 4));
		System.out.println(redisUtils.bitGet(redisOrKey1, 5));
		System.out.println(redisUtils.bitGet(redisOrKey1, 6));
	}

}
