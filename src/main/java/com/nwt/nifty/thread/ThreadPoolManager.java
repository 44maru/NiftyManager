package com.nwt.nifty.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {

	public static List<ExecutorService> threadPoolList = new ArrayList<ExecutorService>();

	public static void allocateThreadPool(int poolNum, int poolSize) {
		for (int i = 0; i < poolNum; i++) {
			threadPoolList.add(Executors.newFixedThreadPool(poolSize));
		}
	}
}
