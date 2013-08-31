package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.io.TcpResponseCommand;

/*================================================================================================*/
public abstract class Command {

    private static final Logger vLog = Logger.getLogger(Command.class);
    
	public static final String SESSION = "session";
	public static final String QUERY = "query";
	public static final String QUERYC = "queryc";
	public static final String CONFIG = "config";
	
	public static final String SESSION_SHORT = "s";
	public static final String QUERY_SHORT = "q";
	public static final String QUERYC_SHORT = "k";
	public static final String CONFIG_SHORT = "c";
	
	public static final String[] AllCommands = new String[] {
		SESSION,
		QUERY,
		QUERYC,
		CONFIG
	};
	
	protected final SessionContext vSessCtx;
	protected final String vCommand;
	protected List<String> vArgs;
	protected List<CommandArgValidator> vValidators;
	protected final TcpResponseCommand vResponse;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected Command(SessionContext pSessCtx, String pCommand, List<String> pArgs,
														List<CommandArgValidator> pValidators) {
		vSessCtx = pSessCtx;
		vCommand = pCommand;
		vArgs = pArgs;
		vValidators = pValidators;
		vResponse = new TcpResponseCommand();
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void execute() {
		long t = System.currentTimeMillis();
		
		try {
			validate();
			executeInner();
		}
		catch ( IllegalArgumentException iae ) {
			vResponse.err = iae.getMessage();
			
			if ( vSessCtx.getConfigDebugMode() ) {
				vSessCtx.logAndPrintErr("// "+iae, vLog, Level.ERROR, iae);
			}
		}
		catch ( Exception e ) {
			vResponse.err = e.getClass().getName()+"> "+e.getMessage();

			if ( vSessCtx.getConfigDebugMode() ) {
				vSessCtx.logAndPrintErr("// "+e, vLog, Level.ERROR, e);
			}
		}
		
		vResponse.timer = System.currentTimeMillis()-t;
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void validate() throws IllegalArgumentException {
		if ( vArgs.size() > vValidators.size() ) {
			throw new IllegalArgumentException("Too many arguments for command '"+vCommand+"'.");
		}
		
		for ( CommandArgValidator v : vValidators ) {
			v.validateArgs(vCommand, vArgs);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected abstract void executeInner() throws Exception;

	/*--------------------------------------------------------------------------------------------*/
	public TcpResponseCommand getResponse() {
		return vResponse;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static Command build(SessionContext pSessCtx, String pCommand, List<String> pArgs)
																throws IllegalArgumentException {
		if ( isSessionCommand(pCommand) ) {
			return new SessionCommand(pSessCtx, pArgs);
		}

		if ( isQueryCommand(pCommand) ) {
			return new QueryCommand(pSessCtx, pArgs);
		}

		if ( isQuerycCommand(pCommand) ) {
			return new QuerycCommand(pSessCtx, pArgs);
		}

		if ( isConfigCommand(pCommand) ) {
			return new ConfigCommand(pSessCtx, pArgs);
		}
		
		return new ExceptionCommand(pSessCtx,
			new IllegalArgumentException("Unknown command '"+pCommand+"'."));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public static List<CommandArgValidator> argumentValidators(String pCommand)
																throws IllegalArgumentException {
		if ( isSessionCommand(pCommand) ) {
			return SessionCommand.Validators;
		}

		if ( isQueryCommand(pCommand) ) {
			return QueryCommand.Validators;
		}
		
		if ( isQuerycCommand(pCommand) ) {
			return QuerycCommand.Validators;
		}

		if ( isConfigCommand(pCommand) ) {
			return ConfigCommand.Validators;
		}
		
		return new ArrayList<CommandArgValidator>();
	}

	
	/*--------------------------------------------------------------------------------------------*/
	public static List<String> availableArguments(String pCommand, int pArgIndex) {
		if ( isSessionCommand(pCommand) ) {
			switch ( pArgIndex ) {
				case 0: return SessionCommand.Arg0s;
			}
		}

		if ( isConfigCommand(pCommand) ) {
			switch ( pArgIndex ) {
				case 0: return ConfigCommand.Arg0s;
			}
		}
		
		return new ArrayList<String>();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean isSessionCommand(String pCommand) {
		return (pCommand.equals(SESSION) || pCommand.equals(SESSION_SHORT));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean isQueryCommand(String pCommand) {
		return (pCommand.equals(QUERY) || pCommand.equals(QUERY_SHORT));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean isQuerycCommand(String pCommand) {
		return (pCommand.equals(QUERYC) || pCommand.equals(QUERYC_SHORT));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean isConfigCommand(String pCommand) {
		return (pCommand.equals(CONFIG) || pCommand.equals(CONFIG_SHORT));
	}
	
}