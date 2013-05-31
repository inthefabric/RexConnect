package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.SessionContext;

/*================================================================================================*/
public class ExceptionCommand extends Command {
	
    private static final Logger vLog = Logger.getLogger(ExceptionCommand.class);
    
    
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
			vSessCtx.logAndPrintErr("// "+pException, vLog, Level.ERROR, pException);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {}
	
}