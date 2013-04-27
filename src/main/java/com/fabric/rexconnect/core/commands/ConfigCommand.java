package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

import com.fabric.rexconnect.core.SessionContext;

/*================================================================================================*/
public class ConfigCommand extends Command {

	public static final String DEBUG = "debug";
	public static final String PRETTY = "pretty";

	public static final int DEBUG_MODE_OFF = 0;
	public static final int DEBUG_MODE_ON = 1;
	public static final int PRETTY_MODE_OFF = 0;
	public static final int PRETTY_MODE_ON = 1;
	
	public static final List<String> Arg0s = InitArg0s();
	public static final List<CommandArgValidator> Validators = InitValidators();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, "setting", CommandArgValidator.StringType, true,Arg0s));
		vals.add(new CommandArgValidator(1, "mode", CommandArgValidator.IntType, true));
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
	public ConfigCommand(SessionContext pSessCtx, List<String> pArgs) {
		super(pSessCtx, Command.CONFIG, pArgs, Validators);
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
			vSessCtx.setConfigDebugMode(mode == DEBUG_MODE_ON);
		}
		else if ( setting.equals(PRETTY) ) {
			vSessCtx.setConfigPrettyMode(mode == PRETTY_MODE_ON);
		}
	}
	
}