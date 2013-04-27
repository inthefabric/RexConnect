package com.fabric.rexconnect.main;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.CommandArgValidator;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponseCommand;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class RexConnectConsole {

	private static SessionContext vSessCtx;
	private static ObjectMapper vObjMapper;
	private static JsonFactory vJsonFactory;


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static void main(String args[]) throws Exception {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ERROR);

			Properties props = RexConnectServer.buildRexConfig();
			RexConnectServer.printHeader("Console", props);

			vSessCtx = new SessionContext(RexConnectServer.RexConfig);
			vJsonFactory = new JsonFactory();

			vObjMapper = new ObjectMapper();
			vObjMapper.setSerializationInclusion(Include.NON_NULL);

			while ( true ) {
				String cmd = commandPrompt();
				List<CommandArgValidator> cmdArgVals = Command.argumentValidators(cmd);
				List<String> cmdArgs = new ArrayList<String>();

				for ( CommandArgValidator v : cmdArgVals ) {
					cmdArgs.add(commandArgPrompt(v));
				}
				
				System.out.println();
				executeAndPrint(cmd, cmdArgs);
				System.out.println();
			}
		}
		catch ( Exception e ) {
			System.err.println();
			System.err.println("RexConnectConsole Error:");
			System.err.println();
			throw e;
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	private static void executeAndPrint(String pCmd, List<String> pCmdArgs) throws Exception {
		Command command = Command.build(vSessCtx, pCmd, pCmdArgs);
		command.execute();
		TcpResponseCommand resp = command.getResponse();

		////

		StringWriter sw = new StringWriter();
		JsonGenerator jg = vJsonFactory.createJsonGenerator(sw);

		if ( vSessCtx.getConfigPrettyMode() ) {
			jg.setPrettyPrinter(new PrettyJson());
		}

		vObjMapper.writeValue(jg, resp);
		System.out.println(sw.toString());
		Thread.sleep(10); //helps to sync stderr output
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static String commandPrompt() throws IOException {
		System.out.print("# RexConnect> ");
		return readLine();
	}

	/*--------------------------------------------------------------------------------------------*/
	private static String commandArgPrompt(CommandArgValidator pArgVal) throws IOException {
		System.out.print("#  - "+pArgVal.toPromptString()+": ");
		return readLine();
	}

	/*--------------------------------------------------------------------------------------------*/
	private static String readLine() throws IOException {
		Console c = System.console();

		if ( c == null ) { //for Eclipse
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		}
		
		return c.readLine();
	}

}