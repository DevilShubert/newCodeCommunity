package com.example.others;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueTests {
	static private LinkedBlockingQueue<Integer> linkedBlockingQueue;

	public static void main(String[] args) {

		linkedBlockingQueue = new LinkedBlockingQueue<>(20);
		for (int i = 0; i < 5; i++) {
			// 生产者一共5个人，每个人放100个数
			new Thread(new Producer(linkedBlockingQueue)).start();
			// 消费者也是一共5个人，每个人都会拿100个数
			new Thread(new Consumer(linkedBlockingQueue)).start();
		}

	}
}

class Producer implements Runnable {
	private LinkedBlockingQueue<Integer> linkedBlockingQueue;

	public Producer(LinkedBlockingQueue<Integer> linkedBlockingQueue) {
		this.linkedBlockingQueue = linkedBlockingQueue;
	}

	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(new Random().nextInt(50));
				linkedBlockingQueue.put(1);
				System.out.println(
						"放入：当前线程为：" + Thread.currentThread().getName() + "；此时队列长度为： " + linkedBlockingQueue.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Consumer implements Runnable {
	private LinkedBlockingQueue<Integer> linkedBlockingQueue;

	public Consumer(LinkedBlockingQueue<Integer> linkedBlockingQueue) {
		this.linkedBlockingQueue = linkedBlockingQueue;
	}

	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(new Random().nextInt(80));
				linkedBlockingQueue.take();
				System.out.println(
						"取出：当前线程为：" + Thread.currentThread().getName() + "；此时队列长度为： " + linkedBlockingQueue.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
