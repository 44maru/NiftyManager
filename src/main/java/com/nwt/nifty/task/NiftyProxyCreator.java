package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ACCESS_KEY;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ACCOUTING;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ADMIN;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_AVAILABITILY_ZONE;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_IMAGE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_TYPE;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_IP_TYPE;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_KEY_NAME;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_PASSWD;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_SECRET_KEY;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.ClientConfiguration;
import com.nifty.cloud.sdk.auth.BasicCredentials;
import com.nifty.cloud.sdk.auth.Credentials;
import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.DescribeInstancesRequest;
import com.nifty.cloud.sdk.server.model.DescribeInstancesResult;
import com.nifty.cloud.sdk.server.model.Placement;
import com.nifty.cloud.sdk.server.model.Reservation;
import com.nifty.cloud.sdk.server.model.RunInstancesRequest;
import com.nifty.cloud.sdk.server.model.RunInstancesResult;
import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.util.NiftyProxyUtil;

public class NiftyProxyCreator implements NiftyProxyTaskIF {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyCreator.class);

	public TaskResultStatus runTask(String[] srvInfo, int lineNum) {
		try {
			RunInstancesResult result = mkServer(srvInfo, lineNum);
			if (result != null && result.getReservation() != null) {
				log.info("InstanceID {} => サーバ作成完了。 IP => {}", srvInfo[INDEX_INSTANCE_ID],
						result.getReservation().getInstances().get(0).getIpAddress());
				return TaskResultStatus.SUCCESS;
			} else {
				log.error("InstanceID {} => サーバ作成失敗。", srvInfo[INDEX_INSTANCE_ID]);
				return TaskResultStatus.ERROR;
			}
		} catch (Exception e) {
			log.error("InstanceID {} => サーバ作成失敗。", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	public RunInstancesResult mkServer(String[] srvInfo, int lineNum) {

		String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

		if (endpoint == null) {
			log.warn("{}行目, リージョン'{}'は、定義外の値です。処理対象外とします。", lineNum, srvInfo[INDEX_REGION]);
			log.info("利用可能なリージョンは以下です。");
			for (String region : ENDPOINT_MAP.keySet()) {
				log.info(region);
			}
			return null;
		}

		NiftyServerClient client = NiftyProxyUtil.mkClient(srvInfo, endpoint);
		RunInstancesRequest request = mkRequest(srvInfo);

		return client.runInstances(request);
	}

	private RunInstancesRequest mkRequest(String[] srvInfo) {
		RunInstancesRequest request = new RunInstancesRequest();
		request.setImageId(srvInfo[INDEX_IMAGE_ID]);
		request.setKeyName(srvInfo[INDEX_KEY_NAME]);
		request.setInstanceType(srvInfo[INDEX_INSTANCE_TYPE]);
		request.setAccountingType(srvInfo[INDEX_ACCOUTING]);
		request.setInstanceId(srvInfo[INDEX_INSTANCE_ID]);
		request.setAdmin(srvInfo[INDEX_ADMIN]);
		request.setPassword(srvInfo[INDEX_PASSWD]);
		request.setIpType(srvInfo[INDEX_IP_TYPE]);
		Placement placement = new Placement();
		placement.setAvailabilityZone(srvInfo[INDEX_AVAILABITILY_ZONE]);
		request.setPlacement(placement);
		request.setDisableApiTermination(false);
		return request;
	}

	public Reservation execDescribeInstances(String[] srvInfo, String endpoint) {
		try {
			Credentials credential = new BasicCredentials(srvInfo[INDEX_ACCESS_KEY], srvInfo[INDEX_SECRET_KEY]);
			ClientConfiguration config = new ClientConfiguration();
			NiftyServerClient client = new NiftyServerClient(credential, config);
			client.setEndpoint(endpoint);

			DescribeInstancesRequest request = new DescribeInstancesRequest();
			List<String> instanceIds = new ArrayList<String>();
			instanceIds.add(srvInfo[INDEX_INSTANCE_ID]);
			request.setInstanceIds(instanceIds);

			DescribeInstancesResult result = client.describeInstances(request);
			if (result.getReservations() != null) {
				List<Reservation> reservations = result.getReservations();
				return reservations.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
}
