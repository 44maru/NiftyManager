package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_LIST;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwt.nifty.thread.NiftyProxyTotalTaskThread;

import au.com.bytecode.opencsv.CSVReader;

public class NiftyProxyTaskRunner {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyTaskRunner.class);

	private static final String INPUT_CSV = "./input.csv";

	private List<String> instanceIdList = new ArrayList<String>();

	public void runTask(List<NiftyProxyTaskIF> taskList) throws IOException {
		ExecutorService threadPool = Executors.newFixedThreadPool(100);
		List<Future<?>> futList = new ArrayList<Future<?>>();
		int lineNum = 0;

		try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(INPUT_CSV)))) {
			for (String[] rec : reader.readAll()) {
				lineNum++;
				if (lineNum == 1) {
					continue;
				}

				if (!isValidRecord(lineNum, rec)) {
					ProcessCounter.addSkipCnt();
					continue;
				}

				NiftyProxyTotalTaskThread thread = new NiftyProxyTotalTaskThread();
				thread.taskList = taskList;
				thread.srvInfo = rec;
				thread.lineNum = lineNum;
				futList.add(threadPool.submit(thread));
			}
		} catch (FileNotFoundException e) {
			log.error("{}が存在しません。", INPUT_CSV);
		}

		threadPool.shutdown();

		for (Future<?> fut : futList) {
			try {
				fut.get();
			} catch (InterruptedException | ExecutionException e) {
				log.error("スレッド終了待機失敗");
			}
		}

		log.info("データ読込数 {} : 成功数 {} : 失敗数 {} : スキップ数 {}", lineNum - 1, ProcessCounter.getSuccessCnt(),
				ProcessCounter.getErrorCnt(), ProcessCounter.getSkipCnt());
	}

	private boolean isValidRecord(int lineNum, String[] rec) {
		for (int index : INDEX_LIST) {
			if (rec[index] == null || rec[index].isEmpty()) {
				log.warn("{}行目, {}列目の値が未入力です。処理対象外とします。", lineNum, index + 1);
				return false;
			}
		}
		return true;
	}

}
