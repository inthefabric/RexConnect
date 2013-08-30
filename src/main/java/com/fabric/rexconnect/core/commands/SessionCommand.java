package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

import com.fabric.rexconnect.core.WrappedRexsterClient;
import com.fabric.rexconnect.core.SessionContext;

/*================================================================================================*/
public class SessionCommand extends Command {
	
	public static final String START = "start";
	public static final String CLOSE = "close";
	public static final String COMMIT = "commit";
	public static final String ROLLBACK = "rollback";
	
	public static final List<String> Arg0s = InitArg0s();
	public static final List<CommandArgValidator> Validators = InitValidators();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, "action", CommandArgValidator.StringType, true, Arg0s));
		return vals;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static List<String> InitArg0s() {
		List<String> vals = new ArrayList<String>();
		vals.add(START);
		vals.add(CLOSE);
		vals.add(COMMIT);
		vals.add(ROLLBACK);
		return vals;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionCommand(SessionContext pSessCtx, List<String> pArgs) {
		super(pSessCtx, Command.SESSION, pArgs, Validators);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {
		WrappedRexsterClient rcc = vSessCtx.getOrOpenClient();
		String action = vArgs.get(0);
		
		if ( action.equals(START) ) {
			rcc.startSession();
			vResponse.results = new ArrayList<Object>();
			vResponse.results.add(vSessCtx.getSessionId().toString());
		}
		
		if ( action.equals(CLOSE) ) {
			rcc.closeSession();
		}
		
		if ( action.equals(COMMIT) ) {
			rcc.execute("g.commit()", null);
		}
		
		if ( action.equals(ROLLBACK) ) {
			rcc.execute("g.rollback()", null);
		}
	}
	
}