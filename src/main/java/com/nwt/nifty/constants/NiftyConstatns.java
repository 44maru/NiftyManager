package com.nwt.nifty.constants;

import java.util.Map;

public class NiftyConstatns {

	public static final int SRV_STATE_PENDING = 0;
	public static final int SRV_STATE_RUNNING = 16;
	public static final int SRV_STATE_STOPPED = 80;
	public static final int SRV_STATE_WAITING = 112;
	public static final int SRV_STATE_CREATING = 128;

	public static final String REGION_EAST_1 = "east-1";
	public static final String REGION_EAST_2 = "east-2";
	public static final String REGION_EAST_3 = "east-3";
	public static final String REGION_EAST_4 = "east-4";
	public static final String REGION_JP_EAST_4 = "jp-east-4";
	public static final String REGION_WEST_1 = "west-1";
	public static final String REGION_US_EAST_1 = "us-east-1";

	public static final String ENDPOINT_EAST_1 = "https://jp-east-1.computing.api.nifcloud.com/api/";
	public static final String ENDPOINT_EAST_2 = "https://jp-east-2.computing.api.nifcloud.com/api/";
	public static final String ENDPOINT_EAST_3 = "https://jp-east-3.computing.api.nifcloud.com/api/";
	public static final String ENDPOINT_EAST_4 = "https://jp-east-4.computing.api.nifcloud.com/api/";
	public static final String ENDPOINT_WEST_1 = "https://jp-west-1.computing.api.nifcloud.com/api/";
	public static final String ENDPOINT_US_EAST_1 = "https://us-east-1.computing.api.nifcloud.com/api/";

	public static final Map<String, String> ENDPOINT_MAP = Map.of(REGION_EAST_1, ENDPOINT_EAST_1, REGION_EAST_2,
			ENDPOINT_EAST_2, REGION_EAST_3, ENDPOINT_EAST_3, REGION_EAST_4, ENDPOINT_EAST_4, REGION_JP_EAST_4,
			ENDPOINT_EAST_4, REGION_US_EAST_1, ENDPOINT_US_EAST_1, REGION_WEST_1, ENDPOINT_WEST_1);

	public static final Map<Integer, String> SERVER_STATUS_MAP = Map.of(SRV_STATE_PENDING, "PENDING", SRV_STATE_RUNNING,
			"RUNNING", SRV_STATE_STOPPED, "STOPPED", SRV_STATE_WAITING, "WAITING", SRV_STATE_CREATING, "CREATING");

}
