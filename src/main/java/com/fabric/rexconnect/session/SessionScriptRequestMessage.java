package com.fabric.rexconnect.session;

import org.msgpack.annotation.Message;

import com.tinkerpop.rexster.protocol.msg.RexProChannel;
import com.tinkerpop.rexster.protocol.msg.RexProMessageMetaField;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
@Message
public class SessionScriptRequestMessage extends ScriptRequestMessage {
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected RexProMessageMetaField[] getMetaFields() {
		RexProMessageMetaField[] fields = {
			//Execute request in a stateful session
			RexProMessageMetaField.define(META_KEY_IN_SESSION, false, true, Boolean.class),
			
			//Set graph and graph variable name
			RexProMessageMetaField.define(META_KEY_GRAPH_NAME, false, null, String.class),
			RexProMessageMetaField.define(META_KEY_GRAPH_OBJECT_NAME, false, "g", String.class),

			//Variables defined in this request will be available in the next
			RexProMessageMetaField.define(META_KEY_ISOLATE_REQUEST, false, false, Boolean.class),

			//Do not wrap this request in a session
			RexProMessageMetaField.define(META_KEY_TRANSACTION, false, false, Boolean.class),

			//Use the MsgPack serialization channel
			RexProMessageMetaField.define(META_KEY_CHANNEL, false,
				RexProChannel.CHANNEL_MSGPACK, Integer.class)
		};

		return fields;
	}

}
