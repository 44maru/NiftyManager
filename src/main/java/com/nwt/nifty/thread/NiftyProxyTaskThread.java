package com.nwt.nifty.thread;

import java.util.concurrent.Callable;

import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.task.NiftyProxyTaskIF;

public class NiftyProxyTaskThread implements Callable<TaskResultStatus> {

	public NiftyProxyTaskIF task;
	public String[] srvInfo;
	public int lineNum;

	@Override
	public TaskResultStatus call() {
		return task.runTask(srvInfo, lineNum);
	}

}
