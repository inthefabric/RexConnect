package com.fabric.rexconnect.core.io;

import java.util.HashMap;

/*================================================================================================*/
public class TcpResponseCommand extends HashMap<String, String> {

	private static final long serialVersionUID = -468221517265957516L;
	
	public long timer;
	public Object result;
	public String err;
	
}