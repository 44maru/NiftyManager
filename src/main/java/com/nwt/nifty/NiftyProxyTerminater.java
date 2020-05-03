package com.nwt.nifty;

import static com.nwt.constants.CsvIndexConstants.*;
import static com.nwt.constants.NiftyConstatns.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.DescribeInstancesRequest;
import com.nifty.cloud.sdk.server.model.DescribeInstancesResult;
import com.nifty.cloud.sdk.server.model.InstanceState;
import com.nifty.cloud.sdk.server.model.Reservation;
import com.nifty.cloud.sdk.server.model.StopInstancesRequest;
import com.nifty.cloud.sdk.server.model.StopInstancesResult;
import com.nifty.cloud.sdk.server.model.TerminateInstancesRequest;
import com.nifty.cloud.sdk.server.model.TerminateInstancesResult;

public class NiftyProxyTerminater extends NiftyProxyManager {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyTerminater.class);
	private List<TerminateInstance> terminateInstanceList = new ArrayList<TerminateInstance>();
	private boolean is1stCheck = true;

	@Override
	public void controllServer(String[] srvInfo) {
		try {
			if (is1stCheck) {
				log.info("------- サーバステータスチェック開始 -------");
				is1stCheck = false;
			}
			checkServerStatus(srvInfo, counter.getLineNum());
		} catch (Exception e) {
			log.error("InstanceID {} => 想定外エラー発生。処理の対象外とします。", srvInfo[INDEX_INSTANCE_ID], e);
			counter.addErrorCnt();
		}
	}

	public void checkServerStatus(String[] srvInfo, int lineNum) {

		String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

		if (endpoint == null) {
			log.warn("{}行目, リージョン'{}'は、定義外の値です。処理対象外とします。", lineNum + 1, srvInfo[INDEX_REGION]);
			log.info("利用可能なリージョンは以下です。");
			for (String region : ENDPOINT_MAP.keySet()) {
				log.info(region);
			}
			counter.addSkipCnt();
			return;
		}

		NiftyServerClient client = mkClient(srvInfo, endpoint);
		DescribeInstancesRequest request = mkDescribeRequest(srvInfo[INDEX_INSTANCE_ID]);
		DescribeInstancesResult result = client.describeInstances(request);

		if (result.getReservations() != null) {
			nextActionForSrvStatus(srvInfo, client, result);
		} else {
			log.error("InstanceID {} => サーバステータス取得失敗。処理の対象外とします。", srvInfo[INDEX_INSTANCE_ID]);
			counter.addErrorCnt();
		}
	}

	private void nextActionForSrvStatus(String[] srvInfo, NiftyServerClient client, DescribeInstancesResult result) {
		List<Reservation> reservations = result.getReservations();

		InstanceState state = reservations.get(0).getInstances().get(0).getState();

		if (state.getCode() == SRV_STATE_RUNNING) {
			log.info("InstanceID {} => サーバのステータス[起動中]", srvInfo[INDEX_INSTANCE_ID]);
			if (stopInstances(srvInfo, client)) {
				log.info("InstanceID {} => サーバ停止リクエスト送信完了。", srvInfo[INDEX_INSTANCE_ID]);
				terminateInstanceList.add(
						new TerminateInstance(client, srvInfo[INDEX_INSTANCE_ID]));
			} else {
				log.error("InstanceID {} => サーバの停止に失敗しました。削除処理の対象外とします。", srvInfo[INDEX_INSTANCE_ID]);
				counter.addErrorCnt();
			}

		} else if (state.getCode() == SRV_STATE_STOPPED) {
			log.info("InstanceID {} => サーバのステータス[停止中]。", srvInfo[INDEX_INSTANCE_ID]);
			terminateInstanceList.add(
					new TerminateInstance(client, srvInfo[INDEX_INSTANCE_ID]));

		} else {
			log.warn("InstanceID {} => サーバのステータス[{}]。起動中でも停止中でもないため、処理の対象外とします。",
					srvInfo[INDEX_INSTANCE_ID], state);
			counter.addSkipCnt();
		}
	}

	private DescribeInstancesRequest mkDescribeRequest(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

	public boolean stopInstances(String[] srvInfo, NiftyServerClient client) {
		try {
			StopInstancesRequest request = new StopInstancesRequest();
			List<String> instanceIds = new ArrayList<String>();
			instanceIds.add(srvInfo[INDEX_INSTANCE_ID]);
			request.setInstanceIds(instanceIds);

			StopInstancesResult result = client.stopInstances(request);
			if (result.getStoppingInstances() != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public void terminateInstances(String instanceId, NiftyServerClient client) {
		try {
			TerminateInstancesRequest request = mkTerminateRequest(instanceId);
			TerminateInstancesResult result = client.terminateInstances(request);

			if (result.getTerminatingInstances() != null) {
				log.info("InstanceID {} => サーバ削除完了。", instanceId);
				counter.addSuccessCnt();
			} else {
				log.error("InstanceID {} => サーバ削除失敗。", instanceId);
				counter.addErrorCnt();
			}

		} catch (Exception e) {
			log.error("InstanceID {} => サーバ削除失敗。", instanceId, e);
			counter.addErrorCnt();
		}
	}

	private TerminateInstancesRequest mkTerminateRequest(String instanceId) {
		TerminateInstancesRequest request = new TerminateInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

	@Override
	public void controllServer2() {
		log.info("------- サーバ削除処理開始 -------");
		for (TerminateInstance instance : terminateInstanceList) {
			if (checkInstanceStopped(instance.instanceId, instance.client)) {
				terminateInstances(instance.instanceId, instance.client);
			}
		}
	}

	private boolean checkInstanceStopped(String instanceId, NiftyServerClient client) {
		DescribeInstancesRequest request = mkDescribeRequest(instanceId);
		try {
			DescribeInstancesResult result = client.describeInstances(request);

			if (result.getReservations() != null) {
				InstanceState state = result.getReservations().get(0).getInstances().get(0).getState();

				if (state.getCode() == SRV_STATE_STOPPED) {
					return true;
				} else {
					return retryWaiting(instanceId, client, request);
				}

			} else {
				log.error("InstanceID {} => サーバステータス取得失敗。処理の対象外とします。", instanceId);
				counter.addErrorCnt();
				return false;
			}

		} catch (Exception e) {
			log.error("InstanceID {} => サーバステータス取得失敗。処理の対象外とします。", instanceId);
			counter.addErrorCnt();
			return false;
		}
	}

	private boolean retryWaiting(String instanceId, NiftyServerClient client, DescribeInstancesRequest request)
			throws InterruptedException {

		DescribeInstancesResult result;
		InstanceState state;
		Date start = new Date();

		log.info("InstanceID {} => サーバの停止処理中。最大1分間、停止を待ちます。", instanceId);

		while (true) {
			Thread.sleep(1000);
			result = client.describeInstances(request);
			state = result.getReservations().get(0).getInstances().get(0).getState();
			if (state.getCode() == SRV_STATE_STOPPED) {
				log.info("InstanceID {} => サーバ停止完了。", instanceId);
				return true;
			}

			Date end = new Date();
			if (end.getTime() - start.getTime() > 60000) {
				log.error("InstanceID {} => 1分経過してもサーバが停止しなかったため、処理の対象外とします。サーバのステータス[{}]",
						instanceId, state);
				counter.addErrorCnt();
				return false;
			}
		}
	}

	private static class TerminateInstance {

		NiftyServerClient client;
		String instanceId;

		public TerminateInstance(NiftyServerClient client, String instanceId) {
			this.client = client;
			this.instanceId = instanceId;
		}
	}
}
