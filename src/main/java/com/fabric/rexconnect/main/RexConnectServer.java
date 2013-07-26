package com.fabric.rexconnect.main;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;

import com.fabric.rexconnect.core.HeartbeatMonitor;
import com.fabric.rexconnect.core.RequestFilter;
import com.fabric.rexconnect.core.RexConnectClient;
import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.netty.NettyServer;
import com.tinkerpop.rexster.client.RexProClientFilter;
import com.tinkerpop.rexster.client.RexsterClientFactory;
import com.tinkerpop.rexster.client.RexsterClientTokens;

/*================================================================================================*/
public class RexConnectServer {

    private static final Logger vLog = Logger.getLogger(RexConnectServer.class);
    
    public static final byte RexProMsgPack = 0;
    public static final byte RexProJson = 1;

    public static BaseConfiguration RexConfig;
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			configureLog4j("server", vLog, Level.WARN);
			Logger.getLogger(RexsterClientFactory.class).setLevel(Level.WARN);
			Logger.getLogger(RexProClientFilter.class).setLevel(Level.INFO);
			
			Properties props = buildRexConfig();
			RexConnectClient.init(RexConfig);
			vLog.info(getHeaderString("Server", props));
			//startGrizzlyServer(props);
			startNettyServer(props, vLog);
			
			//BaseConfiguration hbConfig = (BaseConfiguration)RexConnectServer.RexConfig.clone();
			//hbConfig.setProperty(RexsterClientTokens.CONFIG_TIMEOUT_READ_MS, 1000);
			SessionContext sc = new SessionContext(); //hbConfig);
			
			HeartbeatMonitor hm = new HeartbeatMonitor(sc);
			hm.start();
		}
		catch ( Exception e ) {
			vLog.fatal("RexConnectServer Exception: "+e);
			e.printStackTrace();
		}
    }

    /*--------------------------------------------------------------------------------------------*/
    public static void configureLog4j(String pName, Logger pLog, Level pDefaultLevel)
    																		throws IOException {
		Properties props = new Properties();
		String filename = "log4j."+pName+".properties";
		
		if ( new File(filename).exists() ) {
			FileInputStream fs = new FileInputStream(filename);
			props.load(fs);
			PropertyConfigurator.configure(props);
		}
		else {
			BasicConfigurator.configure();
			pLog.info("No '"+filename+"' file found; using default configuration.");
			Logger.getRootLogger().setLevel(pDefaultLevel);
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
	
			addProperty(RexsterClientTokens.CONFIG_GRAPH_NAME,
				props.getProperty("rexpro_graph_name"));

			addProperty(RexsterClientTokens.CONFIG_TIMEOUT_READ_MS,
					props.getProperty("rexpro_timeout_ms"));

			addProperty(RexsterClientTokens.CONFIG_LANGUAGE, "groovy");
			addProperty(RexsterClientTokens.CONFIG_SERIALIZER, RexProMsgPack);
			addProperty(RexsterClientTokens.CONFIG_GRAPH_OBJECT_NAME, "g");     
			addProperty(RexsterClientTokens.CONFIG_MESSAGE_RETRY_WAIT_MS, 10);

		}};
		
		return props;
    }

    /*--------------------------------------------------------------------------------------------*/
    public static String getHeaderString(String pTitle, Properties pProps) {
    	//Some ASCII, in the Gremlin/Rexster tradition...
    	return "\n"+
			"\"            ---===##\\    \n"+
			"\"                --==##\\  \n"+
			"\"  ---===################>\n"+
			"\"                --==##/  \n"+
			"\"            ---===##/    \n"+
			"\n"+
			"RexConnect "+pTitle+" 0.3.6\n"+
			"\n"+
			pProps+"\n"+
			"\n"+
			"-------------------------------------------------------------\n";
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
		trans.setName("RexConnectServer");
		trans.setProcessor(fcb.build());
		trans.setConnectionTimeout(timeout);
		trans.configureBlocking(false);
		trans.bind(port);
		trans.start();
		
		vLog.info("Server started at port "+port+".");
    }

    /*--------------------------------------------------------------------------------------------*/
    public static void startNettyServer(Properties pProps, Logger pLog) throws Exception {
		int port = Integer.parseInt(pProps.getProperty("rexconnect_port"));
		//int timeout = Integer.parseInt(pProps.getProperty("rexpro_timeout_ms"));
		new NettyServer(port).run();
		pLog.info("RexConnect Netty TCP server started at port "+port+".");
    }
    
}