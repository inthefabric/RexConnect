package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.List;

/*================================================================================================*/
public class QueryCommand extends Command {

	public static final List<CommandArgValidator> Validators = InitValidators();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, CommandArgValidator.StringType, true));
		vals.add(new CommandArgValidator(1, CommandArgValidator.StringType, false));
		return vals;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public QueryCommand(List<String> pArgs) {
		super(Command.QUERY, pArgs, Validators);
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() {
		///
	}
	
}