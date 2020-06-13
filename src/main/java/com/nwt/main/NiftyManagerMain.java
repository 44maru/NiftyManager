package com.nwt.main;

import java.io.IOException;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwt.nifty.NiftyProxyCreator;
import com.nwt.nifty.NiftyProxyShutdown;
import com.nwt.nifty.NiftyProxyStart;
import com.nwt.nifty.NiftyProxyStatus;
import com.nwt.nifty.NiftyProxyTerminater;

import sun.misc.Unsafe;


public class NiftyManagerMain {

	private static final Logger log = LoggerFactory.getLogger(NiftyManagerMain.class);

	public static void main(String[] args) throws IOException {

		disableModileAccessWarning();

		if (args[0].equals("-c")) {
			log.info("�T�[�o�쐬���J�n���܂��B");
			new NiftyProxyCreator().manage();

		} else if (args[0].equals("-d")) {
			log.info("�T�[�o�폜���J�n���܂��B");
			new NiftyProxyTerminater().manage();

		} else if (args[0].equals("-r")) {
			log.info("�T�[�o�N�����J�n���܂��B");
			new NiftyProxyStart().manage();

		} else if (args[0].equals("-s")) {
			log.info("�T�[�o��~���J�n���܂��B");
			new NiftyProxyShutdown().manage();

		} else if (args[0].equals("-i")) {
			log.info("�T�[�o�Ɖ���J�n���܂��B");
			new NiftyProxyStatus().manage();
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
