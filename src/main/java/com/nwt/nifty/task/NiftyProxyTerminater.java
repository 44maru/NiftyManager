package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;
import static com.nwt.nifty.constants.NiftyConstatns.SRV_STATE_RUNNING;
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
import com.nifty.cloud.sdk.server.model.Reservation;
import com.nifty.cloud.sdk.server.model.StopInstancesRequest;
import com.nifty.cloud.sdk.server.model.StopInstancesResult;
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
			log.error("InstanceID {} => �z��O�G���[�����B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	public TaskResultStatus terminateServers(String[] srvInfo, int lineNum) {

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
			return terminateServers(srvInfo, client, result);
		} else {
			log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.ERROR;
		}
	}

	private TaskResultStatus terminateServers(String[] srvInfo, NiftyServerClient client,
			DescribeInstancesResult result) {
		List<Reservation> reservations = result.getReservations();

		InstanceState state = reservations.get(0).getInstances().get(0).getState();

		if (state.getCode() == SRV_STATE_RUNNING) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[�N����]", srvInfo[INDEX_INSTANCE_ID]);
			if (stopInstances(srvInfo, client)) {
				log.info("InstanceID {} => �T�[�o��~���N�G�X�g���M�����B", srvInfo[INDEX_INSTANCE_ID]);
				if (checkInstanceStopped(srvInfo[INDEX_INSTANCE_ID], client)) {
					return terminateInstances(srvInfo[INDEX_INSTANCE_ID], client);
				}
				return TaskResultStatus.ERROR;
			} else {
				log.error("InstanceID {} => �T�[�o�̒�~�Ɏ��s���܂����B�폜�����̑ΏۊO�Ƃ��܂��B", srvInfo[INDEX_INSTANCE_ID]);
				return TaskResultStatus.ERROR;
			}

		} else if (state.getCode() == SRV_STATE_STOPPED) {
			log.info("InstanceID {} => �T�[�o�̃X�e�[�^�X[��~��]�B", srvInfo[INDEX_INSTANCE_ID]);
			return terminateInstances(srvInfo[INDEX_INSTANCE_ID], client);

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

	public TaskResultStatus terminateInstances(String instanceId, NiftyServerClient client) {
		try {
			TerminateInstancesRequest request = mkTerminateRequest(instanceId);
			TerminateInstancesResult result = client.terminateInstances(request);

			if (result.getTerminatingInstances() != null) {
				log.info("InstanceID {} => �T�[�o�폜�����B", instanceId);
				return TaskResultStatus.SUCCESS;
			} else {
				log.error("InstanceID {} => �T�[�o�폜���s�B", instanceId);
				return TaskResultStatus.ERROR;
			}

		} catch (Exception e) {
			log.error("InstanceID {} => �T�[�o�폜���s�B", instanceId, e);
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
				log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", instanceId);
				return false;
			}

		} catch (Exception e) {
			log.error("InstanceID {} => �T�[�o�X�e�[�^�X�擾���s�B�����̑ΏۊO�Ƃ��܂��B", instanceId);
			return false;
		}
	}

	private boolean retryWaiting(String instanceId, NiftyServerClient client, DescribeInstancesRequest request)
			throws InterruptedException {

		DescribeInstancesResult result;
		InstanceState state;
		Date start = new Date();

		log.info("InstanceID {} => �T�[�o�̒�~�������B�ő�1���ԁA��~��҂��܂��B", instanceId);

		while (true) {
			Thread.sleep(5000);
			result = client.describeInstances(request);
			state = result.getReservations().get(0).getInstances().get(0).getState();
			if (state.getCode() == SRV_STATE_STOPPED) {
				log.info("InstanceID {} => �T�[�o��~�����B", instanceId);
				return true;
			}

			Date end = new Date();
			if (end.getTime() - start.getTime() > 60000) {
				log.error("InstanceID {} => 1���o�߂��Ă��T�[�o����~���Ȃ��������߁A�����̑ΏۊO�Ƃ��܂��B�T�[�o�̃X�e�[�^�X[{}]", instanceId, state);
				return false;
			}
		}
	}
}
