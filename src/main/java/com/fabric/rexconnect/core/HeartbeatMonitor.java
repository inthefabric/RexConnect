package com.fabric.rexconnect.core;

/*================================================================================================*/
public class HeartbeatMonitor extends Thread {

	private RexConnectClient vClient;
	private long vTime;
	private boolean vConnecting;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public HeartbeatMonitor(SessionContext pSessCtx) throws Exception {
		super();
		
		vClient = pSessCtx.createClient();
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
		if ( vConnecting ) {
			System.out.println("Attempting to connect to RexPro...");
		}
		
		try {
			long t = System.currentTimeMillis();
			vClient.execute("g", null);
			
			if ( vConnecting ) {
				System.out.println("Connected!");
				System.out.println("");
			}

			System.out.format("------------- Timer: %f days / Heartbeat: %dms\n",
				(t-vTime)/86400000.0, (System.currentTimeMillis()-t));
			
			vConnecting = false;
			sleep(9000);
		}
		catch ( Exception e ) {
			vConnecting = true;
		}
		
		try {
			sleep(1000);
		}
		catch ( InterruptedException e ) {
			System.err.println("Heartbeat timer exception: "+e);
		}
	}
    
}
