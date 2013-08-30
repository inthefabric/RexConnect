package com.fabric.rexconnect.core.io;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
public class TcpResponseCommand {

	@JsonProperty("i")
	public String cmdId;

	@JsonProperty("t")
	public Long timer;

	@JsonProperty("r")
	public List<Object> results;

	@JsonProperty("e")
	public String err;
	
}