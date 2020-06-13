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
import com.nifty.cloud.sdk.server.model.InstanceIdSet;
import com.nifty.cloud.sdk.server.model.InstanceState;
import com.nifty.cloud.sdk.server.model.Reservation;
import com.nifty.cloud.sdk.server.model.StartInstancesRequest;
import com.nifty.cloud.sdk.server.model.StartInstancesResult;
import com.nifty.cloud.sdk.server.model.StopInstancesRequest;
import com.nifty.cloud.sdk.server.model.StopInstancesResult;
import com.nifty.cloud.sdk.server.model.TerminateInstancesRequest;
import com.nifty.cloud.sdk.server.model.TerminateInstancesResult;

public class NiftyProxyStart extends NiftyProxyManager {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyStart.class);

	@Override
	public void controllServer(String[] srvInfo, int lineNum) {
		try {
			startServer(srvInfo, lineNum);
		} catch (Exception e) {
			log.error("InstanceID {} => �z��O�G���[�����B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID], e);
			counter.addErrorCnt();
		}
	}

	public void startServer(String[] srvInfo, int lineNum) {

		String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

		if (endpoint == null) {
			log.warn("{}�s��, ���[�W����'{}'�́A��`�O�̒l�ł��B�����ΏۊO�Ƃ��܂��B", lineNum + 1, srvInfo[INDEX_REGION]);
			log.info("���p�\�ȃ��[�W�����͈ȉ��ł��B");
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
			log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID]);
			counter.addErrorCnt();
		}
	}

	private void nextActionForSrvStatus(String[] srvInfo, NiftyServerClient client, DescribeInstancesResult result) {
		List<Reservation> reservations = result.getReservations();

		InstanceState state = reservations.get(0).getInstances().get(0).getState();

		if (state.getCode() == SRV_STATE_RUNNING) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[�N����]", srvInfo[INDEX_INSTANCE_ID]);

		} else if (state.getCode() == SRV_STATE_STOPPED) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[��~��]�B", srvInfo[INDEX_INSTANCE_ID]);
			if (startInstances(srvInfo, client)) {
				log.info("InstanceID {} => �T�[�o�N�����N�G�X�g���M�����B", srvInfo[INDEX_INSTANCE_ID]);
				if (checkInstanceRunning(srvInfo[INDEX_INSTANCE_ID], client)) {
					terminateInstances(srvInfo[INDEX_INSTANCE_ID], client);
				}
			} else {
				log.error("InstanceID {} => �T�[�o�̋N���Ɏ��s���܂����B", srvInfo[INDEX_INSTANCE_ID]);
				counter.addErrorCnt();
			}

		} else {
			log.warn("InstanceID {} => �T�[�o�̃X�e�[�^�X[{}]�B�N�����ł���~���ł��Ȃ����߁A�����̑ΏۊO�Ƃ��܂��B",
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

	public void terminateInstances(String instanceId, NiftyServerClient client) {
		try {
			TerminateInstancesRequest request = mkTerminateRequest(instanceId);
			TerminateInstancesResult result = client.terminateInstances(request);

			if (result.getTerminatingInstances() != null) {
				log.info("InstanceID {} => �T�[�o�폜�����B", instanceId);
				counter.addSuccessCnt();
			} else {
				log.error("InstanceID {} => �T�[�o�폜���s�B", instanceId);
				counter.addErrorCnt();
			}

		} catch (Exception e) {
			log.error("InstanceID {} => �T�[�o�폜���s�B", instanceId, e);
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

	private boolean checkInstanceRunning(String instanceId, NiftyServerClient client) {
		DescribeInstancesRequest request = mkDescribeRequest(instanceId);
		try {
			DescribeInstancesResult result = client.describeInstances(request);

			if (result.getReservations() != null) {
				InstanceState state = result.getReservations().get(0).getInstances().get(0).getState();

				if (state.getCode() == SRV_STATE_RUNNING) {
					return true;
				} else {
					return retryWaiting(instanceId, client, request);
				}

			} else {
				log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B", instanceId);
				counter.addErrorCnt();
				return false;
			}

		} catch (Exception e) {
			log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", instanceId);
			counter.addErrorCnt();
			return false;
		}
	}

	private boolean retryWaiting(String instanceId, NiftyServerClient client, DescribeInstancesRequest request)
			throws InterruptedException {

		DescribeInstancesResult result;
		InstanceState state;
		Date start = new Date();

		log.info("InstanceID {} => �T�[�o�̋N���������B�ő�1���ԁA��~��҂��܂��B", instanceId);

		while (true) {
			Thread.sleep(5000);
			result = client.describeInstances(request);
			state = result.getReservations().get(0).getInstances().get(0).getState();
			if (state.getCode() == SRV_STATE_RUNNING) {
				log.info("InstanceID {} => �T�[�o�N�������B", instanceId);
				return true;
			}

			Date end = new Date();
			if (end.getTime() - start.getTime() > 60000) {
				log.error("InstanceID {} => 1���o�߂��Ă��T�[�o���N�����Ȃ��������߁A�X�e�[�^�X�Ď���������߂܂��B�T�[�o�̃X�e�[�^�X[{}]",
						instanceId, state);
				counter.addErrorCnt();
				return false;
			}
		}
	}
}
