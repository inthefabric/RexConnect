package com.fabric.rexconnect.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.RexConnectClient;
import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.CommandArgValidator;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponseCommand;

/*================================================================================================*/
public class RexConnectConsole {

    private static final Logger vLog = Logger.getLogger(RexConnectConsole.class);
	
	private static SessionContext vSessCtx;
	private static ConsoleReader vReader;
	private static Completer vCurrentCompleter;


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static void main(String args[]) throws Exception {
		try {
			RexConnectServer.configureLog4j("console", vLog, Level.ERROR);
			
			Properties props = RexConnectServer.buildRexConfig();
			RexConnectClient.init(RexConnectServer.RexConfig);
			String header = RexConnectServer.getHeaderString("Console", props);
			System.out.println(header);
			vLog.info(header);
			
			vSessCtx = new SessionContext();
			vSessCtx.setConsoleMode(true);
			
			vReader = new ConsoleReader();

			while ( true ) {
				String cmd = commandPrompt();
				List<CommandArgValidator> cmdArgVals = Command.argumentValidators(cmd);
				List<String> cmdArgs = new ArrayList<String>();

				for ( CommandArgValidator v : cmdArgVals ) {
					cmdArgs.add(commandArgPrompt(cmd, v));
				}
				
				vReader.println();
				executeAndPrint(cmd, cmdArgs);
				vReader.println();
			}
		}
		catch ( Exception e ) {
			vReader.println();
			vReader.println("RexConnectConsole Error:");
			vLog.fatal("RexConnectConsole Error:", e);
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
		vLog.info(json);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static void setCompleter(Completer pComp) {
		if ( vCurrentCompleter != null ) {
			vReader.removeCompleter(vCurrentCompleter);
		}
		
		vCurrentCompleter = pComp;
		vReader.addCompleter(vCurrentCompleter);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static String commandPrompt() throws IOException {
		setCompleter(new StringsCompleter(Command.AllCommands));
		
		final String prompt = "# RexConnect> ";
		final String line = readLine(prompt);
		vLog.info(prompt+line);
		return line;
	}

	/*--------------------------------------------------------------------------------------------*/
	private static String commandArgPrompt(String pCommand, CommandArgValidator pArgVal)
																				throws IOException {
		setCompleter(new StringsCompleter(
			Command.availableArguments(pCommand, pArgVal.getIndex())
		));
		
		final String prompt = "#   ..."+pArgVal.toPromptString()+": ";
		final String line = readLine(prompt);
		vLog.info(prompt+line);
		return line;
	}

	/*--------------------------------------------------------------------------------------------*/
	private static String readLine(String pPrompt) throws IOException {
		return vReader.readLine(pPrompt).trim();
	}

}