package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;
import static com.nwt.nifty.constants.NiftyConstatns.SERVER_STATUS_MAP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.DescribeInstancesRequest;
import com.nifty.cloud.sdk.server.model.DescribeInstancesResult;
import com.nifty.cloud.sdk.server.model.InstanceState;
import com.nwt.nifty.constants.TaskResultStatus;
import com.nwt.nifty.util.NiftyProxyUtil;

public class NiftyProxyStatusCheker implements NiftyProxyTaskIF {

	private static final Logger log = LoggerFactory.getLogger(NiftyProxyStatusCheker.class);

	public int expectedStatus;

	public NiftyProxyStatusCheker(int expectedStatus) {
		this.expectedStatus = expectedStatus;
	}

	public TaskResultStatus runTask(String[] srvInfo, int lineNum) {
		try {
			String endpoint = ENDPOINT_MAP.get(srvInfo[INDEX_REGION]);

			if (endpoint == null) {
				log.warn("{}�s��, ���[�W����'{}'�́A��`�O�̒l�ł��B�����ΏۊO�Ƃ��܂��B", lineNum, srvInfo[INDEX_REGION]);
				log.info("���p�\�ȃ��[�W�����͈ȉ��ł��B");
				for (String region : ENDPOINT_MAP.keySet()) {
					log.info(region);
				}
				return TaskResultStatus.ERROR;
			}

			NiftyServerClient client = NiftyProxyUtil.mkClient(srvInfo, endpoint);
			DescribeInstancesRequest request = mkDescribeRequest(srvInfo[INDEX_INSTANCE_ID]);
			DescribeInstancesResult result = client.describeInstances(request);
			return waitForExpectedStatus(srvInfo[INDEX_INSTANCE_ID], client, request);

		} catch (Exception e) {
			log.error("InstanceID {} => �z��O�G���[�����B", srvInfo[INDEX_INSTANCE_ID], e);
			return TaskResultStatus.ERROR;
		}
	}

	private DescribeInstancesRequest mkDescribeRequest(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		request.setInstanceIds(instanceIds);
		return request;
	}

	private TaskResultStatus waitForExpectedStatus(String instanceId, NiftyServerClient client,
			DescribeInstancesRequest request) throws InterruptedException {

		DescribeInstancesResult result;
		InstanceState state;
		Date start = new Date();

		log.info("InstanceID {} => �T�[�o�̂̃X�e�[�^�X��{}�ɂȂ�܂ŁA�ő�1���ԑ҂��܂��B", instanceId);

		while (true) {
			Thread.sleep(5000);
			result = client.describeInstances(request);
			state = result.getReservations().get(0).getInstances().get(0).getState();
			log.info("InstanceID {} => �T�[�o�X�e�[�^�X[{}]", instanceId, state);
			if (state.getCode() == expectedStatus) {
				return TaskResultStatus.SUCCESS;
			}

			Date end = new Date();
			if (end.getTime() - start.getTime() > 60000) {
				log.error("InstanceID {} => 1���o�߂��Ă��T�[�o�X�e�[�^�X��{}�ɂȂ�Ȃ������̂ŁA�Ď���������߂܂��B�T�[�o�̃X�e�[�^�X[{}]", instanceId,
						SERVER_STATUS_MAP.get(expectedStatus), state);
				return TaskResultStatus.ERROR;
			}
		}
	}
}
