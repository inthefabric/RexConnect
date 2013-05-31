package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.rexster.RexsterClient;
import com.fabric.rexconnect.rexster.RexsterClientDelegate;
import com.fabric.rexconnect.rexster.RexsterClientFactory;
import com.fabric.rexconnect.rexster.RexsterClientTokens;
import com.tinkerpop.rexster.client.RexProException;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;
import com.tinkerpop.rexster.protocol.msg.SessionRequestMessage;
import com.tinkerpop.rexster.protocol.msg.SessionResponseMessage;

/*================================================================================================*/
public class RexConnectClient extends RexsterClientDelegate {

    private static final Logger vLog = Logger.getLogger(RexConnectClient.class);
    
	private SessionContext vSessCtx;
	private RexsterClient vClient;
	private Configuration vConfig;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected RexConnectClient(SessionContext pSessCtx, RexsterClient pClient,
																		Configuration pConfig) {
		vSessCtx = pSessCtx;
		vClient = pClient;
		vClient.setDelegate(this);
		vConfig = pConfig;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public <T> List<T> execute(final String pScript, final Map<String, Object> pArgs)
															throws RexProException, IOException {
		return vClient.execute(pScript, pArgs);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void closeConnections() throws RexProException, IOException {
		vClient.closeConnections();
		vClient.close();
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionResponseMessage startSession() throws RexProException, IOException {
		SessionRequestMessage sr = new SessionRequestMessage();
		sr.Channel = vConfig.getInt(RexsterClientTokens.CONFIG_CHANNEL);
		sr.setSessionAsUUID(UUID.randomUUID());
		sr.setRequestAsUUID(UUID.randomUUID());
		sr.metaSetGraphObjName(vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME));
		
		RexProMessage rpm = vClient.execute(sr);
		
		if ( !(rpm instanceof SessionResponseMessage) ) {
			throw new IOException("Invalid response type: "+rpm);
		}

		vSessCtx.openSession(rpm.sessionAsUUID());
		return (SessionResponseMessage)rpm;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public SessionResponseMessage closeSession() throws RexProException, IOException {
		SessionRequestMessage sr = new SessionRequestMessage();
		sr.Channel = vConfig.getInt(RexsterClientTokens.CONFIG_CHANNEL);
		sr.setSessionAsUUID(vSessCtx.getSessionId());
		sr.setRequestAsUUID(UUID.randomUUID());
		sr.metaSetGraphObjName(vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME));
		sr.metaSetKillSession(true);
		
		RexProMessage rpm = vClient.execute(sr);
		
		if ( !(rpm instanceof SessionResponseMessage) ) {
			throw new IOException("Invalid response type: "+rpm);
		}
		
		vSessCtx.closeSession();
		return (SessionResponseMessage)rpm;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	// RexsterClientDelegate methods
	/*--------------------------------------------------------------------------------------------*/
	public void onRequest(RexProMessage pMsg) {
		printMessage("Request", pMsg);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void onResponse(RexProMessage pMsg) {
		printMessage("Response", pMsg);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void printMessage(String pTitle, RexProMessage pMsg) {
		if ( !vSessCtx.getConfigDebugMode() ) {
			return;
		}
		
		String text = pMsg.getClass().getName()+
			(pMsg.hasSession() ? "; Session="+pMsg.sessionAsUUID() : "");
		vSessCtx.logAndPrint("// "+pTitle+": "+text, vLog, Level.DEBUG);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void updateScriptRequestMessage(ScriptRequestMessage pMsg) {
		Boolean s = vSessCtx.isSessionOpen();
		pMsg.metaSetInSession(s);
		pMsg.metaSetIsolate(!s);
		
		if ( s ) {
			pMsg.setSessionAsUUID(vSessCtx.getSessionId());
			pMsg.metaSetGraphObjName(null);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static RexConnectClient create(SessionContext pSessCtx, Configuration pConfig)
																				throws Exception {
		RexsterClient rc = RexsterClientFactory.open(pConfig);
		return new RexConnectClient(pSessCtx, rc, pConfig);
	}

}