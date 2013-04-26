package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fabric.rexconnect.core.RexConnectClient;
import com.fabric.rexconnect.core.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class QueryCommand extends Command {

	public static final List<CommandArgValidator> Validators = InitValidators();
	
	private Map<String, Object> vParamMap;
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, "script", CommandArgValidator.StringType, true));
		vals.add(new CommandArgValidator(1, "params", CommandArgValidator.StringType, false));
		return vals;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public QueryCommand(SessionContext pSessCtx, List<String> pArgs) {
		super(pSessCtx, Command.QUERY, pArgs, Validators);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void validate() throws IllegalArgumentException {
		super.validate();
		
		if ( vArgs.size() <= 1 ) {
			return;
		}
		
		String params = vArgs.get(1);
		
		if ( params == null || params.length() == 0 ) {
			return;
		}
		
    	try {
   			vParamMap = new ObjectMapper().readValue(params, HashMap.class);
    	}
		catch ( Exception e ) {
			Validators.get(1).throwEx(vCommand,
				"Invalid parameter format: "+e.getMessage(), params);
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {
		RexConnectClient rcc = vSessCtx.createClient();
		
		try {
			vResponse.result = rcc.execute(vArgs.get(0), vParamMap);
		}
		catch ( Exception e ) {
			rcc.close();
			throw e;
		}

		rcc.close();
	}
	
}