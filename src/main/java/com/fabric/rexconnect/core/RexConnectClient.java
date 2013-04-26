package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

import com.fabric.rexconnect.rexster.RexsterClient;
import com.fabric.rexconnect.session.SessionScriptRequestMessage;
import com.tinkerpop.rexster.client.RexProException;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;

/*================================================================================================*/
public class RexConnectClient extends RexsterClient {

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected RexConnectClient(Configuration pConfig, TCPNIOTransport pTrans) {
		super(pConfig, pTrans);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public <T> List<T> execute(final String script, final Map<String, Object> scriptArgs)
															throws RexProException, IOException {
		final RexProMessage reqMsg = createSessionScriptRequest(script, scriptArgs);
		return null;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private SessionScriptRequestMessage createSessionScriptRequest(final String script,
			final Map<String, Object> pScriptArgs) throws IOException, RexProException {
		final SessionScriptRequestMessage m = new SessionScriptRequestMessage();
		m.Script = script;
		/*m.LanguageName = this.language;
		m.metaSetGraphName(this.graphName);
		m.metaSetGraphObjName(this.graphObjName);
		m.metaSetInSession(false);
		m.metaSetChannel(this.channel);
		m.metaSetTransaction(this.transaction);*/
		m.setRequestAsUUID(UUID.randomUUID());
		m.validateMetaData();

		if ( pScriptArgs != null ) {
			m.Bindings.putAll(pScriptArgs);
		}

		return m;
	}

}