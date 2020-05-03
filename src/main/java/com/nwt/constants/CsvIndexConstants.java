package com.nwt.constants;

import java.util.List;

public class CsvIndexConstants {

	public static int INDEX_PASSWD = 0;
	public static int INDEX_IMAGE_ID = 1;
	public static int INDEX_INSTANCE_ID = 2;
	public static int INDEX_SECRET_KEY = 3;
	public static int INDEX_ACCESS_KEY = 4;
	public static int INDEX_REGION = 5;
	public static int INDEX_INSTANCE_TYPE = 6;
	public static int INDEX_ACCOUTING = 7;
	public static int INDEX_KEY_NAME = 8;
	public static int INDEX_ADMIN = 9;
	public static int INDEX_AVAILABITILY_ZONE = 10;
	public static int INDEX_IP_TYPE = 11;

	public static List<Integer> INDEX_LIST = List.of(
			INDEX_PASSWD,
			INDEX_IMAGE_ID,
			INDEX_INSTANCE_ID,
			INDEX_SECRET_KEY,
			INDEX_ACCESS_KEY,
			INDEX_REGION,
			INDEX_INSTANCE_TYPE,
			INDEX_ACCOUTING,
			INDEX_KEY_NAME,
			INDEX_ADMIN,
			INDEX_AVAILABITILY_ZONE,
			INDEX_IP_TYPE);
}
