package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;

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
	
	private RexsterClient vClient;
	private Configuration vConfig;
	private SessionContext vSessCtx;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public RexConnectClient(RexsterClient pClient, Configuration pConfig) {
		vClient = pClient;
		vClient.setDelegate(this);
		
		vConfig = pConfig;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public <T> List<T> execute(final SessionContext pSessCtx, final String pScript,
			 				final Map<String, Object> pArgs) throws RexProException, IOException {
		setSessionContext(pSessCtx);
		return vClient.execute(pScript, pArgs);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void close() throws RexProException, IOException {
		closeSession();
		vClient.close();
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected void setSessionContext(SessionContext pSessCtx) throws RexProException, IOException {
		vSessCtx = pSessCtx;
		
		if ( !vSessCtx.useSession() || vSessCtx.isSessionOpen() ) {
			return;
		}
		
		SessionRequestMessage sr = new SessionRequestMessage();
		sr.Channel = vConfig.getInt(RexsterClientTokens.CONFIG_CHANNEL);
		sr.setSessionAsUUID(UUID.randomUUID());
		sr.setRequestAsUUID(UUID.randomUUID());
		sr.metaSetGraphObjName(vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME));
		
		RexProMessage rpm = vClient.execute(sr);
		vSessCtx.openSession(rpm.sessionAsUUID());
		
		if ( !(rpm instanceof SessionResponseMessage) ) {
			throw new IOException("Invalid response type: "+rpm);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void closeSession() throws RexProException, IOException {
		if ( !vSessCtx.useSession() || vSessCtx.isSessionOpen() ) {
			vSessCtx = null;
			return;
		}
		
		SessionRequestMessage sr = new SessionRequestMessage();
		sr.Channel = vConfig.getInt(RexsterClientTokens.CONFIG_CHANNEL);
		sr.setSessionAsUUID(UUID.randomUUID());
		sr.setRequestAsUUID(UUID.randomUUID());
		sr.metaSetGraphObjName(vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME));
		sr.metaSetKillSession(true);
		
		vSessCtx = null;
		RexProMessage rpm = vClient.execute(sr);
		
		if ( !(rpm instanceof SessionResponseMessage) ) {
			throw new IOException("Invalid response type: "+rpm);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected RexProMessage execute(final RexProMessage pMsg) throws RexProException, IOException {
		return vClient.execute(pMsg);
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void onRequest(RexProMessage pMsg) {
		vSessCtx.addRequest(pMsg);
		//System.out.println("Req : "+pMsg.sessionAsUUID());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void onResponse(RexProMessage pMsg) {
		vSessCtx.addResponse(pMsg);
		//System.out.println("Resp: "+pMsg.sessionAsUUID());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void updateScriptRequestMessage(ScriptRequestMessage pMsg) {
		Boolean s = vSessCtx.useSession();
		pMsg.metaSetInSession(s);
		pMsg.metaSetIsolate(!s);
		
		if ( s ) {
			pMsg.setSessionAsUUID(vSessCtx.getSessionId());
			pMsg.metaSetGraphObjName(null);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static RexConnectClient create(Configuration pConfig) throws Exception {
		RexsterClient rc = RexsterClientFactory.open(pConfig);
		return new RexConnectClient(rc, pConfig);
	}

}