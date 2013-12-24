package org.fastcatsearch.ir.field;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.fastcatsearch.ir.field.AStringField;
import org.fastcatsearch.ir.field.AStringMvField;
import org.fastcatsearch.ir.field.DatetimeField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.IntField;
import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldTest {
	protected static Logger logger = LoggerFactory.getLogger(FieldTest.class);
	
	private BytesDataInput write(Field field) throws IOException{
		BytesDataOutput output = new BytesDataOutput();
		field.writeTo(output);
		byte[] array = output.array();
		return new BytesDataInput(array, 0, array.length);
	}
	
	private BytesDataInput writeFixedDataTo(Field field) throws IOException{
		BytesDataOutput output = new BytesDataOutput();
		field.writeFixedDataTo(output);
		byte[] array = output.array();
		return new BytesDataInput(array, 0, array.length);
	}
	
	private BytesDataInput writeRawString(Field field) throws IOException{
		BytesDataOutput output = new BytesDataOutput();
		field.writeRawTo(output);
		byte[] array = output.array();
		return new BytesDataInput(array, 0, array.length);
	}
	
	@Test
	public void testRawField() throws IOException, FieldDataParseException {
		UStringMvField field = new UStringMvField("tags", "강아지", 10);
		field.parseIndexable();
		System.out.println(field.getDataString());
		
	}
	
	@Test
	public void testIntegerField() throws IOException, FieldDataParseException {
		
		String value = "1231435";
		IntField field = new IntField("A", value);
		assertEquals(value, field.rawString()); 
		field.parseIndexable();
		BytesDataInput input = write(field);
		
		IntField field2 = new IntField("A");
		field2.readFrom(input);
		assertEquals(null, field2.rawString()); 
		String value2 = field2.getValue().toString();
		
		assertEquals(value, value2);
		
		System.out.println(value2);
		
	}
	
	@Test
	public void testRawIntegerField() throws IOException, FieldDataParseException {
		
		String value = "4163526";
		IntField field = new IntField("A", value);
		assertEquals(value, field.rawString()); 
		field.parseIndexable();
		BytesDataInput input = writeRawString(field);
		
		IntField field2 = new IntField("A");
		field2.readRawFrom(input);
		assertEquals(value, field2.rawString()); 
		
		field2.parseIndexable();
		String value2 = field2.getValue().toString();
		assertEquals(value, value2);
	}
	
	@Test
	public void testDatetimeField() throws IOException, FieldDataParseException {
		
		String value = "2013-06-12 12:30:11";
		DatetimeField field = new DatetimeField("A", value);
		BytesDataInput input = write(field);
		
		DatetimeField field2 = new DatetimeField("A");
		field2.readFrom(input);
		
		SimpleDateFormat sdfc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = (Date) field2.getValue();
		String value2 = sdfc.format(date);
		
		assertEquals(value, value2);
		
		System.out.println(value2);
		
	}
	
	@Test
	public void testDatetimeFieldFilter() throws IOException, FieldDataParseException {
		
		String value = "2001-11-17 11";
		DatetimeField field = new DatetimeField("A", value);
		BytesDataOutput output = new BytesDataOutput();
		field.writeFixedDataTo(output);
		byte[] array = output.array();
		
		logger.info("{}", array);
		
	}
	
	String avalue = "It really depends on what kind of Stream you're working with. " +
			"For instance System.console().readLine() (new in Java 6) is pretty easy. " +
			"Same with BufferedReader's readLine()";
	
	@Test
	public void testStringField() throws IOException, FieldDataParseException {
		
		
		AStringField field = new AStringField("A", avalue);
		BytesDataInput input = write(field);
		
		AStringField field2 = new AStringField("A");
		field2.readFrom(input);
		String value2 = field2.getValue().toString();
		
		assertEquals(avalue, value2);
		
		System.out.println(value2);
		
	}
	
	@Test
	public void testFixedStringField() throws IOException, FieldDataParseException {
		int size = 10;
		AStringField field = new AStringField("A", avalue, size);
		BytesDataInput input = write(field);
		
		AStringField field2 = new AStringField("A");
		field2.readFrom(input);
		String value2 = field2.getValue().toString();
		
		assertEquals(avalue.substring(0, size), value2);
		
		System.out.println(field.getValue());
		
	}
	
	@Test
	public void testFixedStringMvField() throws IOException, FieldDataParseException {
		int size = 10;
		String[] values = new String[]{"123456789011", "223456789011", "323456789011"};
		AStringMvField field = new AStringMvField("A", size);
		field.addValue(values[0]);
		field.addValue(values[1]);
		field.addValue(values[2]);
		BytesDataInput input = write(field);
		
		AStringMvField field2 = new AStringMvField("A");
		field2.readFrom(input);
		Iterator<Object> iterator = field2.getMultiValueIterator();
		int i = 0;
		while(iterator.hasNext()){
			String val = iterator.next().toString();
			if(values[i].length() > size){
				values[i] = values[i].substring(0, size);
			}
			assertEquals(values[i++], val);
		}
		
		System.out.println(field.getValue());
		
	}
	
	@Test
	public void testRawFixedStringMvField() throws IOException, FieldDataParseException {
		int size = 10;
		String[] values = new String[]{"123456789011", "223456789011", "323456789011"};
		String value = "123456789011,223456789011,323456789011";
		AStringMvField field = new AStringMvField("A", value, size);
		BytesDataInput input = writeRawString(field);
		
		AStringMvField field2 = new AStringMvField("A", size);
		field2.readRawFrom(input);
		
		assertEquals(value, field.rawString());
		assertEquals(value, field2.rawString());
		
		field2.parseIndexable(",");
		
		Iterator<Object> iterator = field2.getMultiValueIterator();
		int i = 0;
		while(iterator.hasNext()){
			String val = iterator.next().toString();
			if(values[i].length() > size){
				values[i] = values[i].substring(0, size);
			}
			assertEquals(values[i++], val);
			System.out.println(val);
		}
		
	}

}
