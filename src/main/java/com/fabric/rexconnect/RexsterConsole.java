package com.fabric.rexconnect;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jline.ConsoleReader;
import jline.History;

import com.tinkerpop.pipes.util.iterators.SingleIterator;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.protocol.RemoteRexsterSession;
import com.tinkerpop.rexster.protocol.ResultAndBindings;
import com.tinkerpop.rexster.protocol.RexsterBindings;
import com.tinkerpop.rexster.protocol.msg.ConsoleScriptResponseMessage;
import com.tinkerpop.rexster.protocol.msg.ErrorResponseMessage;
import com.tinkerpop.rexster.protocol.msg.MessageFlag;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
public class RexsterConsole {

    private static final String REXSTER_HISTORY = ".rexster_history";
    private static final String HOST = "node3.inthefabric.com"; //"127.0.0.1";
    private static final int HOST_PORT = 8184;
	
    private RemoteRexsterSession vSession = null;
    private List<String> vCurrBindings = new ArrayList<String>();
    private final PrintStream vOutput = System.out;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(final String[] args) throws Exception {
        try {
            RexsterConsole rc = new RexsterConsole();
            rc.start();
        }
        catch (Exception ex) {
            die(ex);
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    private static void die(final Throwable pEx) {
        System.out.println(pEx.getMessage() + " (stack trace follows)");
        pEx.printStackTrace();
        System.exit(1);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public RexsterConsole() {
    }

    /*--------------------------------------------------------------------------------------------*/
    public void start() throws Exception {
    	initAndOpenSession();
    	
        if ( this.vSession.isOpen() ) {
            this.vOutput.println("Session open!");
            this.primaryLoop();
        }
        else {
            this.vOutput.println("Could not connect to the Rexster server");
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    public void initAndOpenSession() {
    	this.vOutput.println("Opening session at "+HOST+":"+HOST_PORT);
        this.vSession = new RemoteRexsterSession(HOST, HOST_PORT, 100, null, null);
        this.vSession.open();
        this.initSessionVars();
    }
    
    /*--------------------------------------------------------------------------------------------*/
    private void resetSessionWithRexster() {
        this.vOutput.println("Resetting session "+HOST+":"+HOST_PORT);
        
        if ( this.vSession != null ) {
            this.vSession.reset();
            this.initSessionVars();
        }
        else {
            this.initAndOpenSession();
        }

        this.vCurrBindings.clear();
        this.vOutput.println("--> done");
    }

    /*--------------------------------------------------------------------------------------------*/
    private void initSessionVars() {
        executeScript("g = rexster.getGraph('Fabric');");
    }
    
    /*--------------------------------------------------------------------------------------------*/
    private void closeConsole() {
        this.vOutput.print("Closing session "+HOST+":"+HOST_PORT);
        
        if (this.vSession != null) {
            this.vSession.close();
            this.vSession = null;
        }
        
        this.vOutput.println("--> done");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    private void primaryLoop() throws Exception {
        final ConsoleReader reader = getInputReader();

        String line = "";
        this.vOutput.println();

        while ( line != null ) {
            try {
                line = "";
                boolean submit = false;
                boolean newline = false;
                
                while ( !submit ) {
                    if ( newline ) {
                        line = line + "\n" + reader.readLine(
                        	RexsterConsole.makeSpace(this.getPrompt().length()));
                    }
                    else {
                        line = line + "\n" + reader.readLine(this.getPrompt());
                    }
                    
                    if ( line.endsWith(" .") ) {
                        newline = true;
                        line = line.substring(0, line.length() - 2);
                    }
                    else {
                        line = line.trim();
                        submit = true;
                    }
                }

                if ( line.isEmpty() ) {
                    continue;
                }
                
                if ( line.equals(Tokens.REXSTER_CONSOLE_QUIT) ) {
                    this.closeConsole();
                    return;
                }
                else if ( line.equals(Tokens.REXSTER_CONSOLE_HELP) ) {
                    this.printHelp();
                }
                else if ( line.equals(Tokens.REXSTER_CONSOLE_RESET) ) {
                    this.resetSessionWithRexster();
                }
                else {
                    executeScript(line);
                }
            }
            catch ( Exception e ) {
                this.vOutput.println("Evaluation error: " + e.getMessage());
            }
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    private ConsoleReader getInputReader() throws IOException {
        final ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);
        reader.setUseHistory(true);

        try {
            final History history = new History();
            history.setHistoryFile(new File(REXSTER_HISTORY));
            reader.setHistory(history);
        }
        catch ( IOException e ) {
            System.err.println("Could not find history file");
        }
        
        return reader;
    }

    /*--------------------------------------------------------------------------------------------*/
    public String getPrompt() {
        return "RexConnect> ";
    }

    /*--------------------------------------------------------------------------------------------*/
    public static String makeSpace(final int number) {
        String space = "";
        
        for (int i = 0; i < number; i++) {
            space = space + " ";
        }
        
        return space;
    }
    
    /*--------------------------------------------------------------------------------------------*/
    public void printHelp() {
        this.vOutput.println("-= Console Specific =-");
        this.vOutput.println(Tokens.REXSTER_CONSOLE_RESET + ": reset the rexster session");
        this.vOutput.println(Tokens.REXSTER_CONSOLE_QUIT + ": quit");
        this.vOutput.println(Tokens.REXSTER_CONSOLE_HELP + ": displays this message");
        this.vOutput.println("");
        this.vOutput.println("-= Rexster Context =-");
        this.vOutput.println("rexster.getGraph(String graphName)");
        this.vOutput.println("rexster.getGraphNames()");
        this.vOutput.println("rexster.getVersion()");
        this.vOutput.println("");
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    private void executeScript(final String pLine) {
        executeScript(pLine, true);
    }

    /*--------------------------------------------------------------------------------------------*/
    private void executeScript(final String pLine, final boolean pShowPrefix) {
    	final long t = System.currentTimeMillis();
        final ResultAndBindings result = eval(pLine, this.vSession);
        final Iterator iter;
        
        if ( result.getResult() instanceof Iterator ) {
            iter = (Iterator)result.getResult();
        }
        else if ( result.getResult() instanceof Iterable ) {
            iter = ((Iterable)result.getResult()).iterator();
        }
        else if ( result.getResult() instanceof Map ) {
            iter = ((Map)result.getResult()).entrySet().iterator();
        }
        else {
            iter = new SingleIterator(result.getResult());
        }

        while ( iter.hasNext() ) {
            final Object o = iter.next();
            
            if ( o != null ) {
                this.vOutput.println();
                this.vOutput.println((pShowPrefix ? "==> " : "")+o);
                this.vOutput.println((pShowPrefix ? "    " : "")+
                	"{"+(System.currentTimeMillis()-t)+"ms}");
                this.vOutput.println();
            }
        }

        this.vCurrBindings = result.getBindings();
    }

    /*--------------------------------------------------------------------------------------------*/
    private static ResultAndBindings eval(final String pScript, final RemoteRexsterSession pSess) {
        ResultAndBindings returnValue = null;

        try {
            pSess.open();

            // the session field gets set by the RemoteRexsterSession class automatically
            final ScriptRequestMessage scriptMsg = new ScriptRequestMessage();
            final RexsterBindings rb = new RexsterBindings();
            
            scriptMsg.Script = pScript;
            scriptMsg.Bindings = ConsoleScriptResponseMessage.convertBindingsToByteArray(rb);
            scriptMsg.LanguageName = "groovy";
            scriptMsg.Flag = MessageFlag.SCRIPT_REQUEST_IN_SESSION;
            scriptMsg.setRequestAsUUID(UUID.randomUUID());

            final RexProMessage resultMsg = pSess.sendRequest(scriptMsg, 3, 500);
            List<String> lines = new ArrayList<String>();
            List<String> bindings = new ArrayList<String>();
            
            try {
                if ( resultMsg instanceof ConsoleScriptResponseMessage ) {
                    final ConsoleScriptResponseMessage responseMessage = (ConsoleScriptResponseMessage)resultMsg;

                    bindings = responseMessage.bindingsAsList();
                    lines = responseMessage.consoleLinesAsList();
                }
                else if ( resultMsg instanceof ErrorResponseMessage ) {
                    final ErrorResponseMessage errorMessage = (ErrorResponseMessage)resultMsg;
                    
                    lines = new ArrayList() {{
                        add(errorMessage.ErrorMessage);
                    }};
                }
            }
            catch ( IllegalArgumentException iae ) {
                ErrorResponseMessage errorMessage = (ErrorResponseMessage) resultMsg;
                lines.add(errorMessage.ErrorMessage);
            }

            Object result = lines.iterator();

            if ( lines.size() == 1 ) {
                result = lines.get(0);
            }

            returnValue = new ResultAndBindings(result, bindings);
        }
        catch ( Exception e ) {
            System.out.println("The session with Rexster Server may have been lost. "+
            	"Please try again or refresh your session with ?r");
        }

        return returnValue;
    }
    
}
