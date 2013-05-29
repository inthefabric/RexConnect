package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.io.TcpResponseCommand;

/*================================================================================================*/
public abstract class Command {
	
	public static final String SESSION = "session";
	public static final String QUERY = "query";
	public static final String CONFIG = "config";
	
	public static final String[] AllCommands = new String[] {
		SESSION,
		QUERY,
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
				System.err.println("// "+iae);
				iae.printStackTrace(System.err);
			}
		}
		catch ( Exception e ) {
			vResponse.err = e.getClass().getName()+"> "+e.getMessage();

			if ( vSessCtx.getConfigDebugMode() ) {
				System.err.println("// "+e);
				e.printStackTrace(System.err);
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
		if ( pCommand.equals(SESSION) ) {
			return new SessionCommand(pSessCtx, pArgs);
		}

		if ( pCommand.equals(QUERY) ) {
			return new QueryCommand(pSessCtx, pArgs);
		}

		if ( pCommand.equals(CONFIG) ) {
			return new ConfigCommand(pSessCtx, pArgs);
		}
		
		return new ExceptionCommand(pSessCtx,
			new IllegalArgumentException("Unknown command '"+pCommand+"'."));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public static List<CommandArgValidator> argumentValidators(String pCommand)
																throws IllegalArgumentException {
		if ( pCommand.equals(SESSION) ) {
			return SessionCommand.Validators;
		}

		if ( pCommand.equals(QUERY) ) {
			return QueryCommand.Validators;
		}

		if ( pCommand.equals(CONFIG) ) {
			return ConfigCommand.Validators;
		}
		
		return new ArrayList<CommandArgValidator>();
	}

	
	/*--------------------------------------------------------------------------------------------*/
	public static List<String> availableArguments(String pCommand, int pArgIndex) {
		if ( pCommand.equals(SESSION) ) {
			switch ( pArgIndex ) {
				case 0: return SessionCommand.Arg0s;
			}
		}

		if ( pCommand.equals(CONFIG) ) {
			switch ( pArgIndex ) {
				case 0: return ConfigCommand.Arg0s;
			}
		}
		
		return new ArrayList<String>();
	}
	
}