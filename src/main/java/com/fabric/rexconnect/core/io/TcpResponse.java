package com.fabric.rexconnect.core.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/*================================================================================================*/
@XmlRootElement
public class TcpResponse {
	
	public String reqId;
	public String sessId;
	public long timer;
	public String err;
	public List<TcpResponseCommand> cmdList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public TcpResponse() {
		cmdList = new ArrayList<TcpResponseCommand>();
	}
	
}