package com.fabric.rexconnect.main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;

import com.fabric.rexconnect.core.CommandHandler;
import com.fabric.rexconnect.core.RequestFilter;
import com.fabric.rexconnect.core.RexConnectClient;
import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.core.netty.NettyServer;
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
@ExtensionNaming(namespace="fabric", name="rexconnect")
public class RexConnectExtension extends AbstractRexsterExtension {

    private static Logger vLog;
    
    
	////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public RexConnectExtension() throws Exception {
    	if ( vLog != null ) {
    		return;
    	}
    	
    	vLog = Logger.getLogger(RexConnectExtension.class);
    	vLog.setLevel(Level.INFO);
    	vLog.info("RexConnect extension starting...");
    	
    	Properties props = RexConnectServer.buildRexConfig();
    	RexConnectClient.init(RexConnectServer.RexConfig);
    	//startGrizzlyServer(props);
    	startNettyServer(props, vLog);

    	vLog.info("RexConnect extension started!");
    }

	/*--------------------------------------------------------------------------------------------*/
	@ExtensionDefinition(extensionPoint=ExtensionPoint.GRAPH)
	public ExtensionResponse execute(@RexsterContext RexsterResourceContext pCtx,
			@RexsterContext Graph pGraph, @ExtensionRequestParameter(name="req") String pReqJson) {
		SessionContext sessCtx = new SessionContext();
		
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

    /*--------------------------------------------------------------------------------------------*/
    private static void startGrizzlyServer(Properties pProps) throws IOException {
		FilterChainBuilder fcb = FilterChainBuilder.stateless();
		fcb.add(new TransportFilter());
		fcb.add(new StringFilter(Charset.forName("UTF-8")));
		fcb.add(new RequestFilter());
		
		int port = Integer.parseInt(pProps.getProperty("rexconnect_port"));
		int timeout = Integer.parseInt(pProps.getProperty("rexpro_timeout_ms"));
		
		final TCPNIOTransport trans = TCPNIOTransportBuilder.newInstance().build();
		trans.setName("RexConnectExtension");
		trans.setProcessor(fcb.build());
		trans.setConnectionTimeout(timeout);
		trans.configureBlocking(false);
		trans.bind(port);
		trans.start();
		
		vLog.info("RexConnect TCP server started at port "+port+".");
    }

    /*--------------------------------------------------------------------------------------------*/
    public static void startNettyServer(Properties pProps, Logger pLog) throws Exception {
		int port = Integer.parseInt(pProps.getProperty("rexconnect_port"));
		//int timeout = Integer.parseInt(pProps.getProperty("rexpro_timeout_ms"));
		new Thread(new NettyServer(port)).start();
		pLog.info("RexConnect Netty TCP server started at port "+port+".");
    }
    
}