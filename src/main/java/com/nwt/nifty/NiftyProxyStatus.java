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

public class NiftyProxyStatus extends NiftyProxyManager {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyStatus.class);

	@Override
	public void controllServer(String[] srvInfo, int lineNum) {
		try {
			showStatus(srvInfo, lineNum);
		} catch (Exception e) {
			log.error("InstanceID {} => 想定外エラー発生。", srvInfo[INDEX_INSTANCE_ID], e);
			counter.addErrorCnt();
		}
	}

	public void showStatus(String[] srvInfo, int lineNum) {

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
			InstanceState state = result.getReservations().get(0).getInstances().get(0).getState();
			String ip = result.getReservations().get(0).getInstances().get(0).getIpAddress();
			log.info("{},{},{}",
					srvInfo[INDEX_INSTANCE_ID], state, ip);
			counter.addErrorCnt();
		} else {
			log.error("InstanceID {} => サーバステータス取得失敗。処理の対象外とします。", srvInfo[INDEX_INSTANCE_ID]);
			counter.addErrorCnt();
		}
	}

	private DescribeInstancesRequest mkDescribeRequest(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

}
