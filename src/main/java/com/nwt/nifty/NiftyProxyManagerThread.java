package com.nwt.nifty;

public class NiftyProxyManagerThread implements Runnable{
	
	public NiftyProxyManagerIF niftyManager;
	public String[] srvInfo;
	public int lineNum;

	@Override
	public void run() {
		niftyManager.controllServer(srvInfo, lineNum);
	}

}
