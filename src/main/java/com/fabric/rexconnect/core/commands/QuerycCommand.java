package com.fabric.rexconnect.core.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fabric.rexconnect.core.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class QuerycCommand extends Command {

    private static final ObjectMapper vMapper = new ObjectMapper();
    
	public static final List<CommandArgValidator> Validators = InitValidators();
	
	private Map<String, Object> vParamMap;
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, "cacheKey", CommandArgValidator.IntType, true));
		vals.add(new CommandArgValidator(1, "params", CommandArgValidator.StringType, false));
		return vals;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public QuerycCommand(SessionContext pSessCtx, List<String> pArgs) {
		super(pSessCtx, Command.QUERYC, pArgs, Validators);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void validate() throws IllegalArgumentException {
		super.validate();
		
		if ( vArgs.size() <= 1 ) {
			return;
		}
		
		String params = vArgs.get(1);
		
		if ( params != null && !params.isEmpty() ) {
			try {
	   			vParamMap = vMapper.readValue(params, HashMap.class);
	    	}
			catch ( Exception e ) {
				Validators.get(1).throwEx(vCommand,
					"Invalid parameter format: "+e.getMessage(), params);
			}
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {
		String script;
		
		try {
			script = QueryCommandCache.GetScriptByKey(Validators.get(0).toInt(vArgs));
		}
		catch ( IOException e ) {
			vResponse.err = e.getMessage();
			return;
		}
		
		vResponse.results = vSessCtx.getOrOpenClient().execute(script, vParamMap);
	}

}