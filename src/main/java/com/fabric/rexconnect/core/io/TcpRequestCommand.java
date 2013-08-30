package com.fabric.rexconnect.core.io;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*================================================================================================*/
public class TcpRequestCommand {
	
	public enum TcpRequestCommandOption {
		OMIT_TIMER(1),
		OMIT_RESULTS(2);
		
		public int value;
		private TcpRequestCommandOption(int pValue) { value = pValue; }
	};
	
	@JsonProperty("i")
	public String cmdId;
	
	@JsonProperty("o")
	public byte opt;

	@JsonProperty("e")
	public List<String> cond;

	@JsonProperty("c")
	public String cmd;

	@JsonProperty("a")
	public List<String> args;

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public Boolean isOptionEnabled(TcpRequestCommandOption pOption) {
		return ((opt & pOption.value) != 0);
	}
	
}