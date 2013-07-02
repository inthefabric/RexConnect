package com.fabric.rexconnect.core.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fabric.rexconnect.core.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.RexsterApplicationGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionMethod;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.protocol.EngineController;
import com.tinkerpop.rexster.protocol.EngineHolder;
import com.tinkerpop.rexster.util.ElementHelper;

/*================================================================================================*/
public class QueryCommand extends Command {

    private static final Logger vLog = Logger.getLogger(QueryCommand.class);
    private static final ObjectMapper vMapper = new ObjectMapper();
    
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
   			vParamMap = vMapper.readValue(params, HashMap.class);
    	}
		catch ( Exception e ) {
			Validators.get(1).throwEx(vCommand,
				"Invalid parameter format: "+e.getMessage(), params);
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void executeInner() throws Exception {
		final RexsterResourceContext ctx = vSessCtx.getRexsterResourceContext();
		final MetricRegistry mr = ctx.getMetricRegistry();
		final Timer timeQuery = mr.timer(MetricRegistry.name("rexconn", "query"));
		final Counter countSucc = mr.counter(MetricRegistry.name("rexconn", "query", "success"));
		final Counter countFail = mr.counter(MetricRegistry.name("rexconn", "query", "fail"));
		
		final ScriptEngine scriptEngine = EngineController.getInstance()
			.getEngineByLanguageName("groovy").getEngine();
		final Bindings bindings = scriptEngine.createBindings();
		bindings.put("g", vSessCtx.getGraph());
		
		for ( Entry<String, Object> pair : vParamMap.entrySet() ) {
			bindings.put(pair.getKey(), pair.getValue());
		}

		final RexsterApplicationGraph rag = ctx.getRexsterApplicationGraph();
		final Timer.Context timeCtx = timeQuery.time();
		
		try {
			Object result = scriptEngine.eval(vArgs.get(0), bindings);
			
			if ( result instanceof List<?> ) {
				vResponse.results = (List<Object>)result;
			}
			else {
				vResponse.results = new ArrayList<Object>();
				vResponse.results.add(result);
			}
			
			countSucc.inc();
		}
		catch ( Exception e ) {
			vLog.error("QueryCommand error:"+e.getMessage(), e);
			vResponse.err = e.getMessage();
			countFail.inc();
		}
		finally {
			timeCtx.stop();
		}
	}
	
}