package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;

import com.fabric.rexconnect.core.SessionContext;

/*================================================================================================*/
public class ExceptionCommand extends Command {
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public ExceptionCommand(SessionContext pSessCtx, Exception pException) {
		super(pSessCtx, "exception", new ArrayList<String>(), new ArrayList<CommandArgValidator>());

		if ( pException instanceof IllegalArgumentException ) {
			vResponse.err = pException.getMessage();
		}
		else {
			vResponse.err = pException.getClass().getName()+"> "+pException.getMessage();
		}

		if ( vSessCtx.getConfigDebugMode() ) {
			System.err.println("// "+pException);
			pException.printStackTrace(System.err);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {}
	
}