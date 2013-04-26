package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

/*================================================================================================*/
public class SessionCommand extends Command {
	
	public static final String START = "start";
	public static final String COMMIT = "commit";
	public static final String ROLLBACK = "rollback";
	
	public static final List<CommandArgValidator> Validators = InitValidators();
	public static final List<String> Arg0s = InitArg0s();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, CommandArgValidator.StringType, true, Arg0s));
		return vals;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static List<String> InitArg0s() {
		List<String> vals = new ArrayList<String>();
		vals.add(START);
		vals.add(COMMIT);
		vals.add(ROLLBACK);
		return vals;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionCommand(List<String> pArgs) {
		super(Command.SESSION, pArgs, Validators);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() {
		String setting = vArgs.get(0);
		
		if ( setting.equals(START) ) {
			return;
		}
		
		if ( setting.equals(COMMIT) ) {
			return;
		}
		
		if ( setting.equals(ROLLBACK) ) {
			return;
		}
	}
	
}