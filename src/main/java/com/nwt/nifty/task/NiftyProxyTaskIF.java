package com.nwt.nifty.task;

import com.nwt.nifty.constants.TaskResultStatus;

public interface NiftyProxyTaskIF {
	public TaskResultStatus runTask(String[] srvInfo, int lineNum);
}
