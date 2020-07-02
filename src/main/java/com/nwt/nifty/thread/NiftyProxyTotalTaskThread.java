package com.nwt.nifty.thread;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.task.NiftyProxyTaskIF;
import com.nwt.nifty.task.ProcessCounter;

public class NiftyProxyTotalTaskThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyTotalTaskThread.class);

	public String[] srvInfo;
	public int lineNum;
	public int expectedServerStatus;
	public List<NiftyProxyTaskIF> taskList;

	@Override
	public void run() {
		for (int i = 0; i < taskList.size(); i++) {
			NiftyProxyTaskThread thread = new NiftyProxyTaskThread();
			thread.task = taskList.get(i);
			thread.srvInfo = srvInfo;
			thread.lineNum = lineNum;
			Future<TaskResultStatus> fut = ThreadPoolManager.threadPoolList.get(i).submit(thread);
			try {
				TaskResultStatus status = fut.get();
				if (status == TaskResultStatus.ERROR) {
					ProcessCounter.addErrorCnt();
					return;
				} else if (status == TaskResultStatus.SKIP) {
					ProcessCounter.addSkipCnt();
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				log.error("タスク実行結果待ち中にエラーが発生しました。", e);
				ProcessCounter.addErrorCnt();
				return;
			}
		}
		ProcessCounter.addSuccessCnt();
	}
}
