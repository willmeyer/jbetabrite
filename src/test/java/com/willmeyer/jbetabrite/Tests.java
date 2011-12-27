package com.willmeyer.jbetabrite;

import org.junit.*;

public class Tests 
{
	public Sign sign = null;
	
	private void setupSign(String port) throws Exception {
		sign = new Sign(port);
		sign.connect();
    }

	private void shutdownSign() throws Exception {
		sign.disconnect();
		sign = null;
    }

	@Before
    public void beforeTest() throws Exception {
		this.setupSign("COM5");
    }

	@After
    public void afterTest() throws Exception {
		this.shutdownSign();
    }

	@Test
	public void testConnectDisconnect() throws Exception 
    {
		// The pre and post test steps do this...
    }

	@Test
	public void testSimpleText() throws Exception 
    {
		try {
			Thread.sleep(500);
			sign.writeSingleMessage("This is a test!!!");
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
	
	public static void main(String[] args) {
		Tests tests = new Tests();
		try {
			tests.beforeTest();
			tests.testSimpleText();
			tests.afterTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
