package com.fabric.rexconnect.main;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionConfiguration;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;
import com.tinkerpop.rexster.extension.RexsterExtension;

/*================================================================================================*/
@ExtensionNaming(namespace="inthefabric", name="rexconnect")
//@Path("/rexconnect")
public class RexConnectExtension implements RexsterExtension {
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public RexConnectExtension() {}
    
	/*--------------------------------------------------------------------------------------------*/
	@Override
	public boolean isConfigurationValid(ExtensionConfiguration pConfig) {
		return true;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	@ExtensionDefinition(extensionPoint=ExtensionPoint.GRAPH)
	@ExtensionDescriptor(description="A simple ping extension.")
	public ExtensionResponse execute(@RexsterContext RexsterResourceContext pCtx,
			@RexsterContext Graph pGraph, @ExtensionRequestParameter(name="req") String pReq) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("test", "hello!");
		map.put("req", pReq);
		return ExtensionResponse.ok(map);
	}
	
}