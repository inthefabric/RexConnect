package com.fabric.rexconnect.core.io;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class PrettyJson implements PrettyPrinter {
	
	private static ObjectMapper vObjMapper;
	private static JsonFactory vJsonFactory;
	private static final String NewLine = System.getProperty("line.separator");
	private int vDepth;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static String getJson(Object pObj, boolean pPretty) throws IOException {
		if ( vJsonFactory == null ) {
			vJsonFactory = new JsonFactory();
			
			vObjMapper = new ObjectMapper();
			vObjMapper.setSerializationInclusion(Include.NON_NULL);
		}
		
		StringWriter sw = new StringWriter();
		JsonGenerator jg = vJsonFactory.createJsonGenerator(sw);

		if ( pPretty ) {
			jg.setPrettyPrinter(new PrettyJson());
		}

		vObjMapper.writeValue(jg, pObj);
		return sw.toString();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public PrettyJson() {
		vDepth = 0;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public String indent() {
		char[] bytes = new char[vDepth*4];
		Arrays.fill(bytes, ' ');
		return new String(bytes);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void writeRootValueSeparator(JsonGenerator pGen)
													throws IOException, JsonGenerationException {
		pGen.writeRaw(' ');
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeStartObject(JsonGenerator pGen) throws IOException, JsonGenerationException {
		pGen.writeRaw("{"+NewLine);
		++vDepth;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeEndObject(JsonGenerator pGen, int pEntryCount)
													throws IOException, JsonGenerationException {
		--vDepth;
		pGen.writeRaw(NewLine+indent()+"}");
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeObjectEntrySeparator(JsonGenerator pGen)
													throws IOException, JsonGenerationException {
		pGen.writeRaw(", "+NewLine+indent());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeObjectFieldValueSeparator(JsonGenerator pGen)
													throws IOException, JsonGenerationException {
		pGen.writeRaw(": ");
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeStartArray(JsonGenerator pGen) throws IOException, JsonGenerationException {
		pGen.writeRaw("["+NewLine);
		++vDepth;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeEndArray(JsonGenerator pGen, int pValueCount)
													throws IOException, JsonGenerationException {
		--vDepth;
		pGen.writeRaw(NewLine+indent()+"]");
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void writeArrayValueSeparator(JsonGenerator pGen)
													throws IOException, JsonGenerationException {
		pGen.writeRaw(", "+NewLine+indent());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void beforeArrayValues(JsonGenerator pGen) throws IOException, JsonGenerationException {
		pGen.writeRaw(indent());
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void beforeObjectEntries(JsonGenerator pGen) throws IOException, JsonGenerationException{
		pGen.writeRaw(indent());
	}
	
}