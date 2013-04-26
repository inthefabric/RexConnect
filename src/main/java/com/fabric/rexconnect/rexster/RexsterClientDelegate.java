package com.fabric.rexconnect.rexster;

import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/**
 * Delegate class to provide "hooks" into various aspects of the RexsterClient.
 *
 * @author Zach Kinstner (github.com/zachkinstner)
 */
public abstract class RexsterClientDelegate {
	
	public abstract void updateScriptRequestMessage(ScriptRequestMessage msg);
	
}