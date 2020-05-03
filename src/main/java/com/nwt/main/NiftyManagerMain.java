package com.nwt.main;

import java.io.IOException;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwt.nifty.NiftyProxyCreator;
import com.nwt.nifty.NiftyProxyTerminater;

public class NiftyManagerMain {

	private static final Logger log = LoggerFactory.getLogger(NiftyManagerMain.class);

	public static void main(String[] args) throws IOException {

		disableModileAccessWarning();

		if (args[0].equals("-c")) {
			log.info("サーバ作成を開始します。");
			new NiftyProxyCreator().manage();

		} else if (args[0].equals("-d")) {
			log.info("サーバ削除を開始します。");
			new NiftyProxyTerminater().manage();
		}
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
