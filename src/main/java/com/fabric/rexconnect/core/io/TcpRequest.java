package com.fabric.rexconnect.core.io;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
public class TcpRequest {

	public enum Option {
		OMIT_TIMER(1);
		
		public int value;
		private Option(int pValue) { value = pValue; }
	};
	
	@JsonProperty("i")
	public String reqId;

	@JsonProperty("s")
	public String sessId;

	@JsonProperty("o")
	public byte opt;
	
	@JsonProperty("c")
	public List<TcpRequestCommand> cmdList;


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public Boolean isOptionEnabled(Option pOption) {
		return ((opt & pOption.value) != 0);
	}
	
}