package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fabric.rexconnect.main.RexConnectServer;
import com.tinkerpop.rexster.client.RexsterClient;
import com.tinkerpop.rexster.client.RexsterClientFactory;

/*================================================================================================*/
public class GremlinExecutor {
	
	private RexsterClient vClient;

    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public String execute(String pScript, Map<String, Object> pParamMap) throws Exception {
		List<Object> list;
		
		try {
			list = getList(pScript, pParamMap);
		}
		catch ( Exception e ) {
			closeClient();
			throw e;
		}
		
		closeClient();
		
		StringBuilder s = new StringBuilder();
		int n = (list == null ? 0 : list.size());
		
		for ( int i = 0 ; i < n ; ++i ) {
			s.append((i > 0 ? "," : "")+objToStr(list.get(i)));
		}
		
		return "["+s.toString()+"]";
	}
	

    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected List<Object> getList(String pScript, Map<String, Object> pParamMap) throws Exception {
		vClient = RexsterClientFactory.open(RexConnectServer.RexConfig);
		return vClient.execute(pScript, pParamMap);
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void closeClient() throws IOException {
		vClient.close();
	}
	

    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private String objToStr(Object pObj) {
		if ( pObj == null ) {
			return "";
		}
		
		if ( pObj instanceof String ) {
			return "\""+(pObj+"").replace("\"", "\\\"")+"\"";
		}
		
		if ( pObj instanceof Number || pObj instanceof Boolean ) {
			return pObj+"";
		}

		if ( pObj instanceof HashMap<?,?> ) {
			StringBuilder s = new StringBuilder();
			HashMap<String,Object> map = (HashMap<String,Object>)pObj;
			int mi = 0;
			
			for ( Map.Entry<String,Object> e : map.entrySet() ) {
				s.append((mi++ > 0 ? "," : "")+"\""+e.getKey()+"\":"+objToStr(e.getValue()));
			}
			
			return "{"+s.toString()+"}";
		}
		
		System.err.println(" # Unhandled object: "+pObj.getClass().getName()+" ... "+pObj);
		return "{"+pObj.toString()+"}";
	}
	
}