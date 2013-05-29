package com.fabric.rexconnect.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jline.console.ConsoleReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.CommandArgValidator;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponseCommand;

/*================================================================================================*/
public class RexConnectConsole {

	private static SessionContext vSessCtx;
	private static ConsoleReader vReader;


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static void main(String args[]) throws Exception {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ERROR);

			Properties props = RexConnectServer.buildRexConfig();
			RexConnectServer.printHeader("Console", props);

			vSessCtx = new SessionContext(RexConnectServer.RexConfig);
			vReader = new ConsoleReader();

			while ( true ) {
				String cmd = commandPrompt();
				List<CommandArgValidator> cmdArgVals = Command.argumentValidators(cmd);
				List<String> cmdArgs = new ArrayList<String>();

				for ( CommandArgValidator v : cmdArgVals ) {
					cmdArgs.add(commandArgPrompt(v));
				}
				
				vReader.println();
				executeAndPrint(cmd, cmdArgs);
				vReader.println();
			}
		}
		catch ( Exception e ) {
			vReader.println();
			vReader.println("RexConnectConsole Error:");
			vReader.println();
			throw e;
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	private static void executeAndPrint(String pCmd, List<String> pCmdArgs) throws Exception {
		Command command = Command.build(vSessCtx, pCmd, pCmdArgs);
		command.execute();
		TcpResponseCommand resp = command.getResponse();
		String json = PrettyJson.getJson(resp, vSessCtx.getConfigPrettyMode());
		vReader.println(json);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static String commandPrompt() throws IOException {
		return vReader.readLine("# RexConnect> ");
	}

	/*--------------------------------------------------------------------------------------------*/
	private static String commandArgPrompt(CommandArgValidator pArgVal) throws IOException {
		return vReader.readLine("#   ..."+pArgVal.toPromptString()+": ");
	}

}