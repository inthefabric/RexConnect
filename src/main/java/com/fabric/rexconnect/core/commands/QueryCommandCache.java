package com.fabric.rexconnect.core.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*================================================================================================*/
public class QueryCommandCache {

	private static final Map<Integer, String> ScriptMap = new HashMap<Integer, String>();
	private static Integer NextKey = 0;
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static String GetScriptByKey(Integer pKey) {
		return ScriptMap.get(pKey);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public static Boolean RemoveScriptByKey(Integer pKey) {
		if ( !ScriptMap.containsKey(pKey) ) { //is this necessary?
			return false;
		}
		
		return (ScriptMap.remove(pKey) != null);
	}

	/*--------------------------------------------------------------------------------------------*/
	public static void RemoveAllScripts() {
		ScriptMap.clear();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static Integer PutScript(String pScript) {
		Set<Integer> keys = ScriptMap.keySet();
		
		for ( Integer key : keys ) {
			if ( ScriptMap.get(key).equals(pScript) ) {
				return key;
			}
		}
		
		ScriptMap.put(++NextKey, pScript);
		return new Integer(NextKey); //create copy
	}

}