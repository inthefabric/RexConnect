package com.fabric.rexconnect.main;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.fabric.rexconnect.core.CommandHandler;
import com.fabric.rexconnect.core.SessionContext;
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

    private static final Logger vLog = Logger.getLogger(RexConnectExtension.class);
    

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	@ExtensionDefinition(extensionPoint=ExtensionPoint.GRAPH)
	public ExtensionResponse execute(@RexsterContext RexsterResourceContext pCtx,
			@RexsterContext Graph pGraph, @ExtensionRequestParameter(name="req") String pReqJson) {
		vLog.warn("#### REQUEST: "+pReqJson);
		SessionContext sessCtx = new SessionContext(RexConnectServer.RexConfig);
		
		try {
			TcpResponse resp = CommandHandler.getResponse(sessCtx, pReqJson);
			vLog.warn("#### RESPONSE: "+resp);
			return new ExtensionResponse(Response.ok(resp).build());
		}
		catch ( Exception e ) {
			return ExtensionResponse.error("Failed: "+e);
		}
	}
	
}