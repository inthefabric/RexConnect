package com.fabric.rexconnect;

import org.apache.commons.configuration.BaseConfiguration;
import org.msgpack.template.Templates;

import com.tinkerpop.rexster.client.RexsterClient;
import com.tinkerpop.rexster.client.RexsterClientFactory;

/*================================================================================================*/
public class GremlinQuery2 {

    private RexsterClient vClient;
    private String vInitGraph;
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public GremlinQuery2(BaseConfiguration pRexConfig) throws Exception {
    	vClient = RexsterClientFactory.getInstance().createClient(pRexConfig);
    	vInitGraph = "g = rexster.getGraph('Fabric');";
    }

	/*--------------------------------------------------------------------------------------------*/
	public String execute(String pScript) throws Exception {
		Object raw = vClient.execute(vInitGraph+pScript, Templates.TValue);
		//System.out.println(" * Raw: "+raw);
		return raw.toString();
	}

}