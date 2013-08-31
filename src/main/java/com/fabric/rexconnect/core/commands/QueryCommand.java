package com.fabric.rexconnect.core.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class QueryCommand extends Command {

    private static final ObjectMapper vMapper = new ObjectMapper();
    private static final Logger vLog = Logger.getLogger(QueryCommand.class);
    
	public static final List<CommandArgValidator> Validators = InitValidators();
	public static final int CACHE_OFF = 0;
	public static final int CACHE_ON = 1;
	
	private Map<String, Object> vParamMap;
	private Boolean vCacheScript;
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static List<CommandArgValidator> InitValidators() {
		List<CommandArgValidator> vals = new ArrayList<CommandArgValidator>();
		vals.add(new CommandArgValidator(0, "script", CommandArgValidator.StringType, true));
		vals.add(new CommandArgValidator(1, "params", CommandArgValidator.StringType, false));
		vals.add(new CommandArgValidator(2, "cache", CommandArgValidator.IntType, false));
		return vals;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public QueryCommand(SessionContext pSessCtx, List<String> pArgs) {
		super(pSessCtx, Command.QUERY, pArgs, Validators);
		vCacheScript = false;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void validate() throws IllegalArgumentException {
		super.validate();
		
		////
		
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
		
    	////
    	
		if ( vArgs.size() <= 2 ) {
			return;
		}
		
		String c = vArgs.get(2);
		vCacheScript = (c != null && c.equals("1"));
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {
		String script = vArgs.get(0);
		vResponse.results = vSessCtx.getOrOpenClient().execute(script, vParamMap);
		
		if ( vCacheScript ) {
			vResponse.cacheKey = QueryCommandCache.PutScript(script);
			
			if ( vSessCtx.getConfigDebugMode() ) {
				vLog.debug("//  Cached script. Key="+vResponse.cacheKey+", Script="+script);
			}
		}
	}

}