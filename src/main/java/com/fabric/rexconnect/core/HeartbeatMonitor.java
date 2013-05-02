package com.fabric.rexconnect.core;

/*================================================================================================*/
public class HeartbeatMonitor extends Thread {

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
			System.out.println("Attempting to connect to RexPro...");
		}
		
		try {
			long t = System.currentTimeMillis();
			vSessCtx.getOrOpenClient().execute("g", null);
			
			if ( vConnecting ) {
				System.out.println("Connected!");
				System.out.println();
			}

			System.out.format("# Life %f days,  beat %dms\n",
				(t-vTime)/86400000.0, (System.currentTimeMillis()-t));
			
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
				System.err.println("Heartbeat timer exception: "+e);
			}
		}
	}
    
}
