package com.fabric.rexconnect.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fabric.rexconnect.main.RexConnectServer;

/*================================================================================================*/
public class GremlinExecutor {
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public String execute(SessionContext pSessCtx, String pScript,
												Map<String, Object> pParamMap) throws Exception {
		List<Object> list;
		RexConnectClient rcc = createClient();
		
		if ( pSessCtx == null ) {
			pSessCtx = new SessionContext(false);
		}
		
		try {
			list = rcc.execute(pSessCtx, pScript, pParamMap);
		}
		catch ( Exception e ) {
			rcc.close();
			throw e;
		}
		
		rcc.close();
		
		StringBuilder s = new StringBuilder();
		int n = (list == null ? 0 : list.size());
		
		for ( int i = 0 ; i < n ; ++i ) {
			s.append((i > 0 ? "," : "")+objToStr(list.get(i)));
		}
		
		return "["+s.toString()+"]";
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void commit(SessionContext pSessCtx) throws Exception {
		RexConnectClient rcc = createClient();
		rcc.execute(pSessCtx, "g.commit()", null);
		rcc.close();
	}

	/*--------------------------------------------------------------------------------------------*/
	public void rollback(SessionContext pSessCtx) throws Exception {
		RexConnectClient rcc = createClient();
		rcc.execute(pSessCtx, "g.rollback()", null);
		rcc.close();
	}
	

    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected RexConnectClient createClient() throws Exception {
		return RexConnectClient.create(RexConnectServer.RexConfig);
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