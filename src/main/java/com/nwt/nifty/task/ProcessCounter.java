package com.nwt.nifty.task;

import java.util.concurrent.atomic.AtomicInteger;

public class ProcessCounter {

	private static AtomicInteger successCnt = new AtomicInteger(0);
	private static AtomicInteger errorCnt = new AtomicInteger(0);
	private static AtomicInteger skipCnt = new AtomicInteger(0);

	public static void addSuccessCnt() {
		successCnt.incrementAndGet();
	}

	public static void addErrorCnt() {
		errorCnt.incrementAndGet();
	}

	public static void addSkipCnt() {
		skipCnt.incrementAndGet();
	}

	public static int getSuccessCnt() {
		return successCnt.get();
	}

	public static int getErrorCnt() {
		return errorCnt.get();
	}

	public static int getSkipCnt() {
		return skipCnt.get();
	}

}
