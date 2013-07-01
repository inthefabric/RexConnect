package com.fabric.rexconnect.main;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.fabric.rexconnect.core.CommandHandler;
import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

/*================================================================================================*/
@MetaInfServices
@ExtensionNaming(namespace="fab", name="rexconn")
public class RexConnectExtension extends AbstractRexsterExtension {

    private static Logger vLog;
    
    
	////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public RexConnectExtension() throws Exception {
    	if ( vLog != null ) {
    		return;
    	}
    	
    	vLog = Logger.getLogger(RexConnectExtension.class);
		RexConnectServer.configureLog4j("extension", vLog, Level.WARN);
		//Logger.getLogger(RexsterClientFactory.class).setLevel(Level.WARN);
		//Logger.getLogger(RexProClientFilter.class).setLevel(Level.INFO);
    	RexConnectServer.buildRexConfig();
    }

	/*--------------------------------------------------------------------------------------------*/
	@ExtensionDefinition(extensionPoint=ExtensionPoint.GRAPH)
	public ExtensionResponse execute(@RexsterContext RexsterResourceContext pCtx,
			@RexsterContext Graph pGraph, @ExtensionRequestParameter(name="req") String pReqJson) {
		SessionContext sessCtx = new SessionContext(RexConnectServer.RexConfig);
		
		try {
			TcpResponse resp = CommandHandler.getResponse(sessCtx, pReqJson);
			String json = PrettyJson.getJson(resp, false);
			Response r = Response.ok(json, MediaType.APPLICATION_JSON).build();
			return new ExtensionResponse(r);
		}
		catch ( Exception e ) {
			return ExtensionResponse.error("Failed: "+e);
		}
	}
	
}