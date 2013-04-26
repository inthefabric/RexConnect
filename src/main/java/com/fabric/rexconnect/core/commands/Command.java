package com.fabric.rexconnect.core.commands;

import java.util.List;

/*================================================================================================*/
public abstract class Command {
	
	public static final String SESSION = "session";
	public static final String QUERY = "query";
	public static final String CONFIG = "config";
	
	protected final String vCommand;
	protected List<String> vArgs;
	protected List<CommandArgValidator> vValidators;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected Command(String pCommand, List<String> pArgs, List<CommandArgValidator> pValidators) {
		vCommand = pCommand;
		vArgs = pArgs;
		vValidators = pValidators;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void execute() throws IllegalArgumentException {
		validate();
		executeInner();
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
	protected abstract void executeInner();

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static Command build(String pCommand, List<String> pArgs)
																throws IllegalArgumentException {
		if ( pCommand == SESSION ) {
			return new SessionCommand(pArgs);
		}

		if ( pCommand == QUERY ) {
			return new QueryCommand(pArgs);
		}

		if ( pCommand == CONFIG ) {
			return new ConfigCommand(pArgs);
		}
		
		throw new IllegalArgumentException("Unknown command '"+pCommand+"'.");
	}
	
}