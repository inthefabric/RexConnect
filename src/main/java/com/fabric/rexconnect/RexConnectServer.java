package com.fabric.rexconnect;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.quickserver.net.server.QuickServer;

import com.tinkerpop.rexster.client.RexsterClientTokens;
import com.tinkerpop.rexster.protocol.msg.RexProChannel;

/*================================================================================================*/
public class RexConnectServer {

	public static BaseConfiguration RexConfig;
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			final Properties props = new Properties();
			props.load(new FileInputStream("rexConnectConfig.properties"));
			System.out.println("Starting with configuration:\n"+props+"\n");
			
			RexConfig = new BaseConfiguration() {{
				addProperty(RexsterClientTokens.CONFIG_PORT,
					Integer.parseInt(props.getProperty("rexpro_port")));
				addProperty(RexsterClientTokens.CONFIG_HOSTNAME, props.getProperty("rexpro_hosts"));
				addProperty(RexsterClientTokens.CONFIG_MESSAGE_RETRY_WAIT_MS, 10);
				addProperty(RexsterClientTokens.CONFIG_GRAPH_NAME,
					props.getProperty("rexpro_graph_name"));
				addProperty(RexsterClientTokens.CONFIG_CHANNEL, RexProChannel.CHANNEL_MSGPACK);
			}};
			
			QuickServer qs = new QuickServer();
			qs.setClientCommandHandler(CommandHandler.class.getName());
			qs.setPort(8185);
			qs.setName("RexConnectServer");
			qs.startServer();
			System.out.println("Server started.");
			
			(new HeartbeatMonitor()).start();
		}
		catch ( Exception e ) {
			System.err.println("RexConnectServer Exception: "+e);
			e.printStackTrace();
		}
    }
    
}