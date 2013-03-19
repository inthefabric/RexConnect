package com.fabric.rexconnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.tinkerpop.rexster.protocol.RemoteRexsterSession;
import com.tinkerpop.rexster.protocol.RexsterBindings;
import com.tinkerpop.rexster.protocol.msg.ConsoleScriptResponseMessage;
import com.tinkerpop.rexster.protocol.msg.ErrorResponseMessage;
import com.tinkerpop.rexster.protocol.msg.MessageFlag;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
public class GremlinQuery {

    private static final String HOST = "node3.inthefabric.com"; //"127.0.0.1";
    private static final int HOST_PORT = 8184;

    private RemoteRexsterSession vSession;
    private List<Object> vResults;
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public GremlinQuery() throws Exception {
		vSession = new RemoteRexsterSession(HOST, HOST_PORT, 100, null, null);
		vSession.open();
		
		if ( !vSession.isOpen() ) {
			throw new Exception("Count not open RemoteRexsterSession.");
		}

		executeInner("g = rexster.getGraph('Fabric')");
    }

	/*--------------------------------------------------------------------------------------------*/
	public void execute(String pScript) throws Exception {
		executeInner(pScript);
	}

	/*--------------------------------------------------------------------------------------------*/
	public String getResultListJson() {
		return "["+StringUtils.join(vResults, ',')+"]";
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private void executeInner(final String pLine) throws Exception {
		vResults = new ArrayList<Object>();

		final Object res = getScriptResult(pLine);
		final Iterator iter;
		
		if ( res instanceof Iterator ) {
			iter = (Iterator)res;
		}
		else {
			throw new Exception("Unknown result type: "+
				(res == null ? "null" : res.getClass().getName()));
		}

		while ( iter.hasNext() ) {
			final Object o = iter.next();
			
			if ( o == null ) {
				vResults.add(null);
				continue;
			}
			
			String s = o.toString();
			char one = s.charAt(0);
			String two = s.substring(0, 2);
			
			if ( one == '{' ) {
			
			}
			else if ( two == "v[" ) {
				s = "{_Id:"+s.substring(2, s.length()-1)+",_Type:'v'}";
			}
			else if ( two == "e[" ) {
				int i = s.indexOf(']');
				String rel = s.substring(i+1, s.length()-i-2);
				s = "{_Id:"+s.substring(2, i-3)+",_Type:'e',_Rel:'"+rel+"'}";
			}
			
			vResults.add(s);
			System.out.println(" - Result: "+o+" // "+s);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private Object getScriptResult(final String pScript) throws IOException {
        final RexsterBindings rb = new RexsterBindings();
        
		final ScriptRequestMessage reqMsg = new ScriptRequestMessage();
		reqMsg.Script = pScript;
		reqMsg.Bindings = ConsoleScriptResponseMessage.convertBindingsToByteArray(rb);
		reqMsg.LanguageName = "groovy";
		reqMsg.Flag = MessageFlag.SCRIPT_REQUEST_IN_SESSION;
		reqMsg.setRequestAsUUID(UUID.randomUUID());
		
		final RexProMessage resultMsg = vSession.sendRequest(reqMsg, 3, 500);
		List<String> lines = new ArrayList<String>();
		
		try {
			if ( resultMsg instanceof ConsoleScriptResponseMessage ) {
				final ConsoleScriptResponseMessage resMsg = (ConsoleScriptResponseMessage)resultMsg;
				resMsg.bindingsAsList();
				lines = resMsg.consoleLinesAsList();
			}
			else if ( resultMsg instanceof ErrorResponseMessage ) {
				final ErrorResponseMessage errMsg = (ErrorResponseMessage)resultMsg;
				lines = new ArrayList<String>();
				lines.add(errMsg.ErrorMessage);
			}
		}
		catch ( IllegalArgumentException iae ) {
			ErrorResponseMessage errMsg = (ErrorResponseMessage)resultMsg;
			lines.add(errMsg.ErrorMessage);
		}
		
		return lines.iterator();
	}

}
