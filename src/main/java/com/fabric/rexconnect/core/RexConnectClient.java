package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.fabric.rexconnect.rexster.RexsterClient;
import com.fabric.rexconnect.rexster.RexsterClientDelegate;
import com.tinkerpop.rexster.client.RexProException;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
public class RexConnectClient extends RexsterClientDelegate {
	
	private RexsterClient vClient;
	private SessionContext vSessCtx;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public RexConnectClient(RexsterClient pClient) {
		vClient = pClient;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public <T> List<T> execute(final SessionContext pSessCtx, final String pScript,
			 				final Map<String, Object> pArgs) throws RexProException, IOException {
		vSessCtx = pSessCtx;
		return vClient.execute(pScript, pArgs);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void updateScriptRequestMessage(ScriptRequestMessage pMsg) {
		pMsg.setSessionAsUUID(vSessCtx.getSessionId());
		pMsg.metaSetInSession(vSessCtx.useSession());
		pMsg.metaSetIsolate(!vSessCtx.useSession());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void close() throws IOException {
		vClient.close();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static RexConnectClient create(Configuration pConfig) {
		RexsterClient rc = null; //RexsterClientFactory.open(pConfig);
		return new RexConnectClient(rc);
	}

}