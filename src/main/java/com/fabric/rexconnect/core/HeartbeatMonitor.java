package com.fabric.rexconnect.core;

/*================================================================================================*/
public class HeartbeatMonitor extends Thread {

	private long vTime;
	private boolean vConnecting;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public HeartbeatMonitor() {
		super();
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
			//vGrem.execute(null, "g", null);
			throw new Exception("No query was executed.");
			
			/*
			if ( vConnecting ) {
				System.out.println("Connected!");
				System.out.println("");
			}

			System.out.format("------------- Timer: %f days / Heartbeat: %dms\n",
				(t-vTime)/86400000.0, (System.currentTimeMillis()-t));
			
			vConnecting = false;
			sleep(9000);
			*/
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
