package com.fabric.rexconnect.core.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
@XmlRootElement
public class TcpResponse {
	
	@JsonProperty("i")
	public String reqId;
	
	@JsonProperty("s")
	public String sessId;
	
	@JsonProperty("t")
	public long timer;
	
	@JsonProperty("e")
	public String err;
	
	@JsonProperty("c")
	public List<TcpResponseCommand> cmdList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public TcpResponse() {
		cmdList = new ArrayList<TcpResponseCommand>();
	}
	
}