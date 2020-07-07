package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.StopInstancesRequest;
import com.nifty.cloud.sdk.server.model.StopInstancesResult;
import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.util.NiftyProxyUtil;

public class NiftyProxyShutdown implements NiftyProxyTaskIF {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyShutdown.class);

	private boolean ignoreStopError = false;

	public NiftyProxyShutdown() {
	}

	public NiftyProxyShutdown(boolean ignoreStopError) {
		this.ignoreStopError = ignoreStopError;
	}

	public TaskResultStatus runTask(String[] srvInfo, int lineNum) {
		try {
			return stopServer(srvInfo, lineNum);
		} catch (Exception e) {
			log.error("InstanceID {} => 想定外エラー発生。", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	public TaskResultStatus stopServer(String[] srvInfo, int lineNum) {

		String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

		if (endpoint == null) {
			log.warn("{}行目, リージョン'{}'は、定義外の値です。処理対象外とします。", lineNum, srvInfo[INDEX_REGION]);
			log.info("利用可能なリージョンは以下です。");
			for (String region : ENDPOINT_MAP.keySet()) {
				log.info(region);
			}
			return TaskResultStatus.SKIP;
		}

		NiftyServerClient client = NiftyProxyUtil.mkClient(srvInfo, endpoint);
		if (stopInstances(srvInfo, client)) {
			log.info("InstanceID {} => サーバ停止リクエスト送信完了。", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.SUCCESS;
		} else {
			log.error("InstanceID {} => サーバの停止に失敗しました。", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.ERROR;
		}
	}

	public boolean stopInstances(String[] srvInfo, NiftyServerClient client) {
		try {
			StopInstancesRequest request = new StopInstancesRequest();
			List<String> instanceIds = new ArrayList<String>();
			instanceIds.add(srvInfo[INDEX_INSTANCE_ID]);
			request.setInstanceIds(instanceIds);

			StopInstancesResult result = client.stopInstances(request);
			if (result.getStoppingInstances() != null && result.getStoppingInstances().size() > 0) {
				return true;
			} else {
				if (ignoreStopError) {
					log.warn("InstanceID {} => サーバ停止リクエストに失敗しまいたが、停止済みと判断します。", srvInfo[INDEX_INSTANCE_ID]);
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}
}
