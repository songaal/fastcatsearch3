package org.fastcatsearch.ir.field;

import java.util.Date;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.util.Formatter;

public class FieldDataStringer {
	public static final String NULL_STRING = "NULL";
	
	public static String parse(FieldSetting.Type type, BytesRef byteRef){
		if(type == FieldSetting.Type.INT){
			return parseIntType(byteRef.bytes, byteRef.offset);
		}else if(type == FieldSetting.Type.LONG){
			return parseLongType(byteRef.bytes, byteRef.offset);
		}else if(type == FieldSetting.Type.FLOAT){
			return parseFloatType(byteRef.bytes, byteRef.offset);
		}else if(type == FieldSetting.Type.DOUBLE){
			return parseDoubleType(byteRef.bytes, byteRef.offset);
		}else if(type == FieldSetting.Type.DATETIME){
			return parseDateTimeType(byteRef.bytes, byteRef.offset);
		}else if(type == FieldSetting.Type.STRING){
			return parseUStringType(byteRef.bytes, byteRef.offset, byteRef.length);
		}else if(type == FieldSetting.Type.ASTRING){
			return parseAStringType(byteRef.bytes, byteRef.offset, byteRef.length);
		}
		
		return new String(byteRef.bytes, byteRef.offset, byteRef.length);
	}
	
	private static String parseIntType(byte[] bytes, int offset){
		int value = IOUtil.readInt(bytes, offset);
		if(value == Integer.MIN_VALUE){
			return NULL_STRING;
		}
		return Integer.toString(value);
	}
	
	private static String parseLongType(byte[] bytes, int offset){
		long value = IOUtil.readLong(bytes, offset);
		if(value == Long.MIN_VALUE){
			return NULL_STRING;
		}
		return Long.toString(value);
	}
	
	private static String parseFloatType(byte[] bytes, int offset){
		int value = IOUtil.readInt(bytes, offset);
		if(value == Integer.MIN_VALUE){
			return NULL_STRING;
		}
		return Float.toString(Float.intBitsToFloat(value));
	}
	
	private static String parseDoubleType(byte[] bytes, int offset){
		long value = IOUtil.readLong(bytes, offset);
		if(value == Long.MIN_VALUE){
			return NULL_STRING;
		}
		return Double.toString(Double.longBitsToDouble(value));
	}
	
	private static String parseDateTimeType(byte[] bytes, int offset){
		long value = IOUtil.readLong(bytes, offset);
		if(value == Long.MIN_VALUE){
			return NULL_STRING;
		}
		return Formatter.formatDate(new Date(value));
	}
	
	private static String parseUStringType(byte[] bytes, int offset, int length){
		return new String(IOUtil.readUChars(bytes, offset, length));
	}
	
	private static String parseAStringType(byte[] bytes, int offset, int length){
		return new String(IOUtil.readAChars(bytes, offset, length));
	}
}
