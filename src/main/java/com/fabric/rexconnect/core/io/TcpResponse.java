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
	
}