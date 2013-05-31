package com.fabric.rexconnect.core;

import org.apache.log4j.Logger;

/*================================================================================================*/
public class HeartbeatMonitor extends Thread {

    private static final Logger vLog = Logger.getLogger(HeartbeatMonitor.class);
    
	private SessionContext vSessCtx;
	private long vTime;
	private boolean vConnecting;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public HeartbeatMonitor(SessionContext pSessCtx) throws Exception {
		super();
		vSessCtx = pSessCtx;
		vTime = System.currentTimeMillis();
		vConnecting = true;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void run() {
		while ( true ) {
			heartbeat();
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private void heartbeat() {
		boolean failed = false;
		
		if ( vConnecting ) {
			vLog.info("Attempting to connect to RexPro...");
		}
		
		try {
			long t = System.currentTimeMillis();
			vSessCtx.getOrOpenClient().execute("g", null);
			
			if ( vConnecting ) {
				vLog.info("Connected!");
			}
			
			vLog.debug(String.format("# Life %f days,  beat %dms",
				(t-vTime)/86400000.0, (System.currentTimeMillis()-t)));
			
			vConnecting = false;
			sleep(10000);
		}
		catch ( Exception e ) {
			failed = true;
			vConnecting = true;
		}
		
		if ( failed ) {
			try {
				sleep(1000);
				vSessCtx.closeClientIfExists();
			}
			catch ( Exception e ) {
				vLog.error("Heartbeat timer exception: "+e);
			}
		}
	}
    
}
