package com.nwt.nifty;

import static com.nwt.constants.CsvIndexConstants.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.ClientConfiguration;
import com.nifty.cloud.sdk.auth.BasicCredentials;
import com.nifty.cloud.sdk.auth.Credentials;
import com.nifty.cloud.sdk.server.NiftyServerClient;

import au.com.bytecode.opencsv.CSVReader;

public abstract class NiftyProxyManager {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyManager.class);

	private static final String INPUT_CSV = "./input.csv";

	protected ProcessCounter counter = new ProcessCounter();

	public abstract void controllServer(String[] srvInfo);

	public abstract void controllServer2();

	public void manage() throws IOException {

		try (CSVReader reader = new CSVReader(
				new InputStreamReader(new FileInputStream(INPUT_CSV)))) {
			for (String[] rec : reader.readAll()) {
				if (counter.getLineNum() == 0) {
					counter.addLineNum();
					continue;
				}

				counter.addLineNum();

				if (!isValidRecord(counter.getLineNum(), rec)) {
					counter.addSkipCnt();
					continue;
				}
				controllServer(rec);
			}
		} catch (FileNotFoundException e) {
			log.error("{}が存在しません。", INPUT_CSV);
		}

		controllServer2();

		log.info("データ読込数 {} : 成功数 {} : 失敗数 {} : スキップ数 {}",
				counter.getLineNum() - 1, counter.getSuccessCnt(),
				counter.getErrorCnt(), counter.getSkipCnt());
	}

	private boolean isValidRecord(int lineNum, String[] rec) {
		for (int index : INDEX_LIST) {
			if (rec[index] == null || rec[index].isEmpty()) {
				log.warn("{}行目, {}列目の値が未入力です。処理対象外とします。", lineNum + 1, index + 1);
				return false;
			}
		}
		return true;
	}

	protected NiftyServerClient mkClient(String[] srvInfo, String endpoint) {
		Credentials credential = new BasicCredentials(
				srvInfo[INDEX_ACCESS_KEY], srvInfo[INDEX_SECRET_KEY]);
		ClientConfiguration config = new ClientConfiguration();
		NiftyServerClient client = new NiftyServerClient(credential, config);
		client.setEndpoint(endpoint);
		return client;
	}

}
