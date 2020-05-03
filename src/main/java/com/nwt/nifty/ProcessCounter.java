package com.nwt.nifty;

public class ProcessCounter {

	private int lineNum = 0;
	private int successCnt = 0;
	private int errorCnt = 0;
	private int skipCnt = 0;

	public void addLineNum() {
		lineNum++;
	}

	public void addSuccessCnt() {
		successCnt++;
	}

	public void addErrorCnt() {
		errorCnt++;
	}

	public void addSkipCnt() {
		skipCnt++;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public int getSuccessCnt() {
		return successCnt;
	}

	public void setSuccessCnt(int successCnt) {
		this.successCnt = successCnt;
	}

	public int getErrorCnt() {
		return errorCnt;
	}

	public void setErrorCnt(int errorCnt) {
		this.errorCnt = errorCnt;
	}

	public int getSkipCnt() {
		return skipCnt;
	}

	public void setSkipCnt(int skipCnt) {
		this.skipCnt = skipCnt;
	}

}
