package com.fabric.rexconnect.main;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.quickserver.net.server.QuickServer;

import com.fabric.rexconnect.core.CommandHandler;
import com.fabric.rexconnect.core.HeartbeatMonitor;
import com.tinkerpop.rexster.client.RexsterClientTokens;
import com.tinkerpop.rexster.protocol.msg.RexProChannel;

/*================================================================================================*/
public class RexConnectServer {

	public static BaseConfiguration RexConfig;
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.WARN);
			
			Properties props = buildRexConfig();
			printHeader("Server", props);
			
			QuickServer qs = new QuickServer();
			qs.setClientCommandHandler(CommandHandler.class.getName());
			qs.setPort(8185);
			qs.setName("RexConnectServer");
			qs.startServer();
			System.out.println("Server started.");
			System.out.println("");
			
			HeartbeatMonitor hm = new HeartbeatMonitor();
			hm.start();
		}
		catch ( Exception e ) {
			System.err.println("RexConnectServer Exception: "+e);
			e.printStackTrace();
		}
    }
    
    /*--------------------------------------------------------------------------------------------*/
    public static Properties buildRexConfig() throws Exception {
		final Properties props = new Properties();
		props.load(new FileInputStream("rexConnectConfig.properties"));
		
		RexConfig = new BaseConfiguration() {{

			addProperty(RexsterClientTokens.CONFIG_PORT,
				Integer.parseInt(props.getProperty("rexpro_port")));
	
			addProperty(RexsterClientTokens.CONFIG_HOSTNAME,
				props.getProperty("rexpro_hosts"));
	
			addProperty(RexsterClientTokens.CONFIG_MESSAGE_RETRY_WAIT_MS,
				10);
	
			addProperty(RexsterClientTokens.CONFIG_GRAPH_NAME,
				props.getProperty("rexpro_graph_name"));
			
			addProperty(RexsterClientTokens.CONFIG_CHANNEL,
				RexProChannel.CHANNEL_MSGPACK);
			
			addProperty(RexsterClientTokens.CONFIG_TIMEOUT_READ_MS,
				props.getProperty("rexpro_timeout_ms"));
			
		}};
		
		return props;
    }

    /*--------------------------------------------------------------------------------------------*/
    public static void printHeader(String pTitle, Properties pProps) {
    	//Some ASCII, in the Gremlin/Rexster tradition...
		System.out.println("");
		System.out.println("          ---===##\\    ");
		System.out.println("              --==##\\  ");
		System.out.println("---===################>");
		System.out.println("              --==##/  ");
		System.out.println("          ---===##/    ");
		System.out.println("");
		System.out.println("RexConnect "+pTitle+" 0.3.0");
		System.out.println(pProps+"");
		System.out.println("");
		System.out.println("-------------------------------------------------------------");
		System.out.println("");
    }
    
}