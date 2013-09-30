package org.fastcatsearch.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONWriter;
import org.junit.Test;

public class JSonTest {

	@Test
	public void testArrayValue() throws JSONException, IOException {
		Writer w = new OutputStreamWriter(System.out);
		JSONWriter writer = new JSONWriter(w);
		writer.object().key("key").array().value("1").value("2").endArray().endObject();
		w.close();
	}

	@Test
	public void testArrayObject() throws JSONException, IOException {
		Writer w = new OutputStreamWriter(System.out);
		JSONWriter writer = new JSONWriter(w);
		writer.object().key("key").array()
		.object().key("k1").value("1").endObject()
		.object().key("k2").value("2").endObject()
		.endArray().endObject();
		w.close();
	}

	
}
