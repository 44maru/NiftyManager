package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ACCOUTING;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_TYPE;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;
import static com.nwt.nifty.constants.NiftyConstatns.SRV_STATE_RUNNING;
import static com.nwt.nifty.constants.NiftyConstatns.SRV_STATE_STOPPED;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.DescribeInstancesRequest;
import com.nifty.cloud.sdk.server.model.DescribeInstancesResult;
import com.nifty.cloud.sdk.server.model.InstanceIdSet;
import com.nifty.cloud.sdk.server.model.InstanceState;
import com.nifty.cloud.sdk.server.model.Reservation;
import com.nifty.cloud.sdk.server.model.StartInstancesRequest;
import com.nifty.cloud.sdk.server.model.StartInstancesResult;
import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.util.NiftyProxyUtil;

public class NiftyProxyStart implements NiftyProxyTaskIF {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyStart.class);

	public TaskResultStatus runTask(String[] srvInfo, int lineNum) {
		try {
			return startServer(srvInfo, lineNum);
		} catch (Exception e) {
			log.error("InstanceID {} => �z��O�G���[�����B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	public TaskResultStatus startServer(String[] srvInfo, int lineNum) {

		String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

		if (endpoint == null) {
			log.warn("{}�s��, ���[�W����'{}'�́A��`�O�̒l�ł��B�����ΏۊO�Ƃ��܂��B", lineNum, srvInfo[INDEX_REGION]);
			log.info("���p�\�ȃ��[�W�����͈ȉ��ł��B");
			for (String region : ENDPOINT_MAP.keySet()) {
				log.info(region);
			}
			return TaskResultStatus.SKIP;
		}

		NiftyServerClient client = NiftyProxyUtil.mkClient(srvInfo, endpoint);
		DescribeInstancesRequest request = mkDescribeRequest(srvInfo[INDEX_INSTANCE_ID]);
		DescribeInstancesResult result = client.describeInstances(request);

		if (result.getReservations() != null) {
			return nextActionForSrvStatus(srvInfo, client, result);
		} else {
			log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.ERROR;
		}
	}

	private TaskResultStatus nextActionForSrvStatus(String[] srvInfo, NiftyServerClient client,
			DescribeInstancesResult result) {
		List<Reservation> reservations = result.getReservations();

		InstanceState state = reservations.get(0).getInstances().get(0).getState();

		if (state.getCode() == SRV_STATE_RUNNING) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[�N����]", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.SKIP;

		} else if (state.getCode() == SRV_STATE_STOPPED) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[��~��]�B", srvInfo[INDEX_INSTANCE_ID]);
			if (startInstances(srvInfo, client)) {
				log.info("InstanceID {} => �T�[�o�N�����N�G�X�g���M�����B", srvInfo[INDEX_INSTANCE_ID]);
				return TaskResultStatus.SUCCESS;
			} else {
				log.error("InstanceID {} => �T�[�o�̋N���Ɏ��s���܂����B", srvInfo[INDEX_INSTANCE_ID]);
				return TaskResultStatus.ERROR;
			}

		} else {
			log.warn("InstanceID {} => �T�[�o�̃X�e�[�^�X[{}]�B�N�����ł���~���ł��Ȃ����߁A�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID], state);
			return TaskResultStatus.SKIP;
		}
	}

	private DescribeInstancesRequest mkDescribeRequest(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

	public boolean startInstances(String[] srvInfo, NiftyServerClient client) {
		try {
			StartInstancesRequest request = new StartInstancesRequest();
			List<InstanceIdSet> instanceIdSetList = new ArrayList<InstanceIdSet>();
			InstanceIdSet instanceIdSet = new InstanceIdSet();
			instanceIdSet.setInstanceId(srvInfo[INDEX_INSTANCE_ID]);
			instanceIdSet.setAccountingType(srvInfo[INDEX_ACCOUTING]);
			instanceIdSet.setInstanceType(srvInfo[INDEX_INSTANCE_TYPE]);
			instanceIdSetList.add(instanceIdSet);
			request.setInstances(instanceIdSetList);

			StartInstancesResult result = client.startInstances(request);
			if (result.getStartingInstances() != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
