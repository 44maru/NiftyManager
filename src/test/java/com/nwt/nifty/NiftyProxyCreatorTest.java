package com.nwt.nifty;

import java.io.IOException;

import org.junit.Test;

public class NiftyProxyCreatorTest {

	@Test
	public void testCreate() throws IOException {
		//new NiftyProxyCreator().manage();
		new NiftyProxyTerminater().manage();
	}

}
