package com.fabric.rexconnect.core.io;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;

/*================================================================================================*/
public class PrettyJson implements PrettyPrinter {
	
	private static final String NewLine = System.getProperty("line.separator");
	private int vDepth;
	
	
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