package com.fabric.rexconnect.core.io;

import java.util.List;

/*================================================================================================*/
public class TcpResponse {
	
	public String reqId;
	public String sessId;
	public long timer;
	public Object result;
	public String err;
	public List<TcpResponseCommand> cmdList;
	
	//if "result" doesn't serialize correctly, try:
	//http://stackoverflow.com/questions/16054366/jackson-serialize-and-deserialize-string-property-as-json
	
}