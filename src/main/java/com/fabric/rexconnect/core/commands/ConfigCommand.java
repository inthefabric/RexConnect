package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

/*================================================================================================*/
public class ConfigCommand extends Command {

	public static final String DEBUG = "debug";
	public static final String PRETTY = "pretty";

	public static final int DEBUG_MODE_OFF = 0;
	public static final int DEBUG_MODE_ON = 1;
	public static final int PRETTY_MODE_OFF = 0;
	public static final int PRETTY_MODE_ON = 1;
	
	public static final List<CommandArgValidator> Validators = InitValidators();
	public static final List<String> Arg0s = InitArg0s();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, CommandArgValidator.StringType, true, Arg0s));
		vals.add(new CommandArgValidator(1, CommandArgValidator.IntType, true));
		return vals;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static List<String> InitArg0s() {
		List<String> vals = new ArrayList<String>();
		vals.add(DEBUG);
		vals.add(PRETTY);
		return vals;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public ConfigCommand(List<String> pArgs) {
		super(Command.CONFIG, pArgs, Validators);
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void validate() throws IllegalArgumentException {
		super.validate();
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() {
		String setting = vArgs.get(0);
		int mode = Validators.get(1).toInt(vArgs);
		
		if ( setting.equals(DEBUG) ) {
			//mode
			return;
		}
		
		if ( setting.equals(PRETTY) ) {
			//mode
			return;
		}
	}
	
}