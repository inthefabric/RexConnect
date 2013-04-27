package com.fabric.rexconnect.core.commands;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/*================================================================================================*/
public class CommandArgValidator {
	
	public static final int IntType = 1;
	public static final int LongType = 2;
	public static final int StringType = 3;
	
	protected final int vIndex;
	protected final String vName;
	protected final int vType;
	protected final boolean vRequired;
	protected List<String> vAcceptStrings;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public CommandArgValidator(int pIndex, String pName, int pType, boolean pRequired) {
		vIndex = pIndex;
		vName = pName;
		vType = pType;
		vRequired = pRequired;
		vAcceptStrings = null;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public CommandArgValidator(int pIndex, String pName, int pType, boolean pRequired,
																		List<String> pAccepts) {
		this(pIndex, pName, pType, pRequired);
		vAcceptStrings = pAccepts;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public String toPromptString() {
		String t = "string";
		
		switch ( vType ) {
			case IntType:
				t = "int";
				break;
				
			case LongType:
				t = "long";
				break;
		}
		
		return vName+" ("+vIndex+"; "+t+(vRequired ? "" : "; opt")+")";
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void validateArgs(String pCommand, List<String> pArgs) throws IllegalArgumentException {
		String arg = (pArgs.size() > vIndex ? pArgs.get(vIndex) : null); 
		String t = null;
		
		if ( arg == null ) {
			if ( !vRequired ) {
				return;
			}
			
			throwEx(pCommand, "Missing required argument.", arg);
		}
		
		try {
			switch ( vType ) {
				case IntType:
					t = Integer.class.getName();
					Integer.parseInt(arg);
					break;
					
				case LongType:
					t = Long.class.getName();
					Long.parseLong(arg);
					break;
			}
		}
		catch ( NumberFormatException e ) {
			throwEx(pCommand, "Incorrect argument type, expected type '"+t+"'.", arg);
		}
		
		validateAcceptStrings(pCommand, arg);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	protected void validateAcceptStrings(String pCommand, String pArg) {
		if ( vType != StringType || vAcceptStrings == null ) {
			return;
		}
		
		boolean found = false;
		
		for ( String s : vAcceptStrings ) {
			if ( pArg.equals(s) ) {
				found = true;
				break;
			}
		}
		
		if ( found ) {
			return;
		}
		
		String list = "'"+StringUtils.join(vAcceptStrings, "', '")+"'";
		throwEx(pCommand, "Argument value not supported. Available values: "+list, pArg);
	}

	/*--------------------------------------------------------------------------------------------*/
	public void throwEx(String pCommand, String pMessage, String pArg)
																throws IllegalArgumentException {
		throw new IllegalArgumentException("Invalid '"+vName+"' argument ('"+pArg+"') at index "+
			vIndex+", for command '"+pCommand+"': "+pMessage+". ");
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public int toInt(List<String> pArgs) throws NumberFormatException {
		return Integer.parseInt(pArgs.get(vIndex));
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public long toLong(List<String> pArgs) throws NumberFormatException {
		return Long.parseLong(pArgs.get(vIndex));
	}
	
}