package com.nwt.nifty.task;

import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_ACCOUTING;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_ID;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_INSTANCE_TYPE;
import static com.nwt.nifty.constants.CsvIndexConstants.INDEX_REGION;
import static com.nwt.nifty.constants.NiftyConstatns.ENDPOINT_MAP;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nifty.cloud.sdk.server.NiftyServerClient;
import com.nifty.cloud.sdk.server.model.InstanceIdSet;
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
		if (startInstances(srvInfo, client)) {
			log.info("InstanceID {} => �T�[�o�N�����N�G�X�g���M�����B", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.SUCCESS;
		} else {
			log.error("InstanceID {} => �T�[�o�̋N���Ɏ��s���܂����B", srvInfo[INDEX_INSTANCE_ID]);
			return TaskResultStatus.ERROR;
		}

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
			if (result.getStartingInstances() != null && result.getStartingInstances().size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
