package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;
import static com.nwt.nifty.constants.NiftyConstatns.SRV_STATE_STOPPED;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.DescribeInstancesRequest;
import com.nifty.cloud.sdk.server.model.DescribeInstancesResult;
import com.nifty.cloud.sdk.server.model.InstanceState;
import com.nifty.cloud.sdk.server.model.TerminateInstancesRequest;
import com.nifty.cloud.sdk.server.model.TerminateInstancesResult;
import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.util.NiftyProxyUtil;

public class NiftyProxyTerminater implements NiftyProxyTaskIF {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyTerminater.class);

	public TaskResultStatus runTask(String[] srvInfo, int lineNum) {
		try {
			return terminateServers(srvInfo, lineNum);
		} catch (Exception e) {
			log.error("InstanceID {} => 想定外エラー発生。処理の対象外とします。", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	public TaskResultStatus terminateServers(String[] srvInfo, int lineNum) {

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
		return terminateInstances(srvInfo[INDEX_INSTANCE_ID], client);
	}

	public TaskResultStatus terminateInstances(String instanceId, NiftyServerClient client) {
		try {
			TerminateInstancesRequest request = mkTerminateRequest(instanceId);
			TerminateInstancesResult result = client.terminateInstances(request);

			if (result.getTerminatingInstances() != null && result.getTerminatingInstances().size() > 0) {
				log.info("InstanceID {} => サーバ削除完了。", instanceId);
				return TaskResultStatus.SUCCESS;
			} else {
				log.error("InstanceID {} => サーバ削除失敗。", instanceId);
				return TaskResultStatus.ERROR;
			}

		} catch (Exception e) {
			log.error("InstanceID {} => サーバ削除失敗。", instanceId, e);
			return TaskResultStatus.ERROR;
		}
	}

	private TerminateInstancesRequest mkTerminateRequest(String instanceId) {
		TerminateInstancesRequest request = new TerminateInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

	private boolean retryWaiting(String instanceId, NiftyServerClient client, DescribeInstancesRequest request)
			throws InterruptedException {

		DescribeInstancesResult result;
		InstanceState state;
		Date start = new Date();

		log.info("InstanceID {} => サーバの停止処理中。最大1分間、停止を待ちます。", instanceId);

		while (true) {
			Thread.sleep(5000);
			result = client.describeInstances(request);
			state = result.getReservations().get(0).getInstances().get(0).getState();
			if (state.getCode() == SRV_STATE_STOPPED) {
				log.info("InstanceID {} => サーバ停止完了。", instanceId);
				return true;
			}

			Date end = new Date();
			if (end.getTime() - start.getTime() > 60000) {
				log.error("InstanceID {} => 1分経過してもサーバが停止しなかったため、処理の対象外とします。サーバのステータス[{}]", instanceId, state);
				return false;
			}
		}
	}
}
