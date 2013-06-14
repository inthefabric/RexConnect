package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.rexster.client.RexProException;
import com.tinkerpop.rexster.client.RexsterClient;
import com.tinkerpop.rexster.client.RexsterClientFactory;
import com.tinkerpop.rexster.client.RexsterClientTokens;
import com.tinkerpop.rexster.protocol.msg.ErrorResponseMessage;
import com.tinkerpop.rexster.protocol.msg.MsgPackScriptResponseMessage;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;
import com.tinkerpop.rexster.protocol.msg.SessionRequestMessage;
import com.tinkerpop.rexster.protocol.msg.SessionResponseMessage;

/*================================================================================================*/
public class RexConnectClient {

    private static final Logger vLog = Logger.getLogger(RexConnectClient.class);
    
	private SessionContext vSessCtx;
	private RexsterClient vClient;
	private Configuration vConfig;
	
    private final String vConfigLang;
    private final String vConfigGraphName;
    private final String vConfigGraphObjName;
    private final int vConfigChannel;
    private final boolean vConfigTx;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected RexConnectClient(SessionContext pSessCtx, RexsterClient pClient,
																		Configuration pConfig) {
		vSessCtx = pSessCtx;
		vClient = pClient;
		vConfig = pConfig;
		
        vConfigLang = vConfig.getString(RexsterClientTokens.CONFIG_LANGUAGE);
        vConfigGraphName = vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_NAME);
        vConfigGraphObjName = vConfig.getString(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME);
        vConfigChannel = vConfig.getInt(RexsterClientTokens.CONFIG_CHANNEL);
        vConfigTx = vConfig.getBoolean(RexsterClientTokens.CONFIG_TRANSACTION);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void closeConnections() throws RexProException, IOException {
		vClient.closeClientAndConnections();
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public <T> List<T> execute(final String pScript, final Map<String, Object> pArgs)
															throws RexProException, IOException {
		ScriptRequestMessage srm = buildScriptRequest(pScript, pArgs);
		printMessage("Request", srm);
		RexProMessage rpm = vClient.execute(srm);
		printMessage("Response", rpm);

		if ( rpm instanceof MsgPackScriptResponseMessage ) {
			return parseMsgPackScriptResponse((MsgPackScriptResponseMessage)rpm);
		}
		
		if ( rpm instanceof ErrorResponseMessage ) {
			throwErrorResponse((ErrorResponseMessage)rpm);
		}
		
		throw new IOException("Unknown response type: "+rpm);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public SessionResponseMessage startSession() throws RexProException, IOException {
		SessionRequestMessage srm = buildSessionRequest(UUID.randomUUID());
		
		RexProMessage rpm = vClient.execute(srm);

		if ( rpm instanceof SessionResponseMessage ) {
			vSessCtx.openSession(rpm.sessionAsUUID());
			return (SessionResponseMessage)rpm;
		}
		
		if ( rpm instanceof ErrorResponseMessage ) {
			throwErrorResponse((ErrorResponseMessage)rpm);
		}
		
		throw new IOException("Unknown response type: "+rpm);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public SessionResponseMessage closeSession() throws RexProException, IOException {
		SessionRequestMessage srm = buildSessionRequest(vSessCtx.getSessionId());
		srm.metaSetKillSession(true);
		
		RexProMessage rpm = vClient.execute(srm);

		if ( rpm instanceof SessionResponseMessage ) {
			vSessCtx.closeSession();
			return (SessionResponseMessage)rpm;
		}
		
		if ( rpm instanceof ErrorResponseMessage ) {
			throwErrorResponse((ErrorResponseMessage)rpm);
		}
		
		throw new IOException("Unknown response type: "+rpm);
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    protected ScriptRequestMessage buildScriptRequest(final String pScript,
    									final Map<String, Object> pArgs) throws RexProException {
        final ScriptRequestMessage srm = new ScriptRequestMessage();
        srm.Script = pScript;
        srm.LanguageName = vConfigLang;
        srm.metaSetGraphName(vConfigGraphName);
        srm.metaSetGraphObjName(vConfigGraphObjName);
        srm.metaSetChannel(vConfigChannel);
        srm.metaSetTransaction(vConfigTx);
        srm.setRequestAsUUID(UUID.randomUUID());

		Boolean s = vSessCtx.isSessionOpen();
		srm.metaSetInSession(s);
		srm.metaSetIsolate(!s);
		
		if ( s ) {
			srm.setSessionAsUUID(vSessCtx.getSessionId());
			srm.metaSetGraphObjName(null);
		}

        srm.validateMetaData();

        if ( pArgs != null ) {
            srm.Bindings.putAll(pArgs);
        }

        return srm;
    }
	
	/*--------------------------------------------------------------------------------------------*/
    protected SessionRequestMessage buildSessionRequest(UUID pSessionId) {
		SessionRequestMessage srm = new SessionRequestMessage();
		srm.Channel = vConfigChannel;
		srm.setSessionAsUUID(pSessionId);
		srm.setRequestAsUUID(UUID.randomUUID());
		srm.metaSetGraphObjName(vConfigGraphObjName);
		return srm;
	}

	/*--------------------------------------------------------------------------------------------*/
	private <T> List<T> parseMsgPackScriptResponse(MsgPackScriptResponseMessage pMsg) {
		final List<T> results = new ArrayList<T>();
		Object result = pMsg.Results.get();
		
		if ( result instanceof Iterable ) {
			final Iterator<T> iter = ((Iterable)result).iterator();
			
			while ( iter.hasNext() ) {
				results.add(iter.next());
			}
		}
		else {
			results.add((T)result);
		}
		
		return results;
	}

	/*--------------------------------------------------------------------------------------------*/
	private void throwErrorResponse(ErrorResponseMessage pMsg) throws RexProException {
		throw new RexProException("[ErrorResponse "+pMsg.metaGetFlag()+"] "+pMsg.ErrorMessage);
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
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static RexConnectClient create(SessionContext pSessCtx, Configuration pConfig)
																				throws Exception {
		RexsterClient rc = RexsterClientFactory.open(pConfig);
		return new RexConnectClient(pSessCtx, rc, pConfig);
	}
	
}