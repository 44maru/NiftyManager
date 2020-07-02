package com.nwt.nifty.util;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ACCESS_KEY;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_SECRET_KEY;

import com.nifty.cloud.sdk.ClientConfiguration;
import com.nifty.cloud.sdk.auth.BasicCredentials;
import com.nifty.cloud.sdk.auth.Credentials;
import com.nifty.cloud.sdk.server.NiftyServerClient;

public class NiftyProxyUtil {
	public static NiftyServerClient mkClient(String[] srvInfo, String endpoint) {
		Credentials credential = new BasicCredentials(srvInfo[INDEX_ACCESS_KEY], srvInfo[INDEX_SECRET_KEY]);
		ClientConfiguration config = new ClientConfiguration();
		NiftyServerClient client = new NiftyServerClient(credential, config);
		client.setEndpoint(endpoint);
		return client;
	}
}
