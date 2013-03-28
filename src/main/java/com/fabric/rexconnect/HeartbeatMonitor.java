package com.fabric.rexconnect;

/*================================================================================================*/
public class HeartbeatMonitor extends Thread {

	private GremlinExecutor vGrem;
	private long vTime;
	private boolean vConnecting;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public HeartbeatMonitor() {
		super();
		vGrem = new GremlinExecutor();
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
			vGrem.execute("g", null);
			
			if ( vConnecting ) {
				System.out.println("Connected!");
			}

			System.out.format("------------- Timer: %f days / Heartbeat: %dms\n",
				(t-vTime)/86400000.0, (System.currentTimeMillis()-t));
			
			vConnecting = false;
			sleep(5000);
			return;
		}
		catch ( Exception e ) {
			vConnecting = true;
		}
	}
    
}
