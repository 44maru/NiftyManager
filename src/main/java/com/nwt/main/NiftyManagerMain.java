package com.nwt.main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwt.nifty.constants.NiftyConstatns;
import com.nwt.nifty.task.NiftyProxyCreator;
import com.nwt.nifty.task.NiftyProxyShutdown;
import com.nwt.nifty.task.NiftyProxyStart;
import com.nwt.nifty.task.NiftyProxyStatus;
import com.nwt.nifty.task.NiftyProxyStatusCheker;
import com.nwt.nifty.task.NiftyProxyTaskIF;
import com.nwt.nifty.task.NiftyProxyTaskRunner;
import com.nwt.nifty.task.NiftyProxyTerminater;
import com.nwt.nifty.thread.ThreadPoolManager;

import sun.misc.Unsafe;

public class NiftyManagerMain {

	private static final Logger log = LoggerFactory.getLogger(NiftyManagerMain.class);

	public static void main(String[] args) throws IOException {

		disableModileAccessWarning();
		int threadPoolSize = Integer.parseInt(args[1]);
		List<NiftyProxyTaskIF> taskList = new ArrayList<NiftyProxyTaskIF>();

		if (args[0].equals("-c")) {
			log.info("サーバ作成を開始します。");
			ThreadPoolManager.allocateThreadPool(1, threadPoolSize);
			taskList.add(new NiftyProxyCreator());

		} else if (args[0].equals("-d")) {
			log.info("サーバ削除を開始します。");
			ThreadPoolManager.allocateThreadPool(3, threadPoolSize);
			taskList.add(new NiftyProxyShutdown());
			taskList.add(new NiftyProxyStatusCheker(NiftyConstatns.SRV_STATE_STOPPED));
			taskList.add(new NiftyProxyTerminater());

		} else if (args[0].equals("-r")) {
			log.info("サーバ起動を開始します。");
			ThreadPoolManager.allocateThreadPool(2, threadPoolSize);
			taskList.add(new NiftyProxyStart());
			taskList.add(new NiftyProxyStatusCheker(NiftyConstatns.SRV_STATE_RUNNING));

		} else if (args[0].equals("-s")) {
			log.info("サーバ停止を開始します。");
			ThreadPoolManager.allocateThreadPool(2, threadPoolSize);
			taskList.add(new NiftyProxyShutdown());
			taskList.add(new NiftyProxyStatusCheker(NiftyConstatns.SRV_STATE_STOPPED));

		} else if (args[0].equals("-i")) {
			log.info("サーバ照会を開始します。");
			ThreadPoolManager.allocateThreadPool(1, threadPoolSize);
			taskList.add(new NiftyProxyStatus());
		}

		new NiftyProxyTaskRunner().runTask(taskList);
	}

	public static void disableModileAccessWarning() {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe u = (Unsafe) theUnsafe.get(null);
			Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
			Field logger = cls.getDeclaredField("logger");
			u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
		} catch (Exception e) {
			// ignore
		}
	}

}
