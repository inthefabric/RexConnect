package com.fabric.rexconnect.core.io;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
public class TcpRequest {
	
	@JsonProperty("i")
	public String reqId;

	@JsonProperty("s")
	public String sessId;

	@JsonProperty("c")
	public List<TcpRequestCommand> cmdList;
	
}