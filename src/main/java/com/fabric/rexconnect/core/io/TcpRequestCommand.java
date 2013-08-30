package com.fabric.rexconnect.core.io;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
public class TcpRequestCommand {
	
	@JsonProperty("i")
	public String cmdId;

	@JsonProperty("e")
	public List<String> cond;

	@JsonProperty("c")
	public String cmd;

	@JsonProperty("a")
	public List<String> args;
	
}