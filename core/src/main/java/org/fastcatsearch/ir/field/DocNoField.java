package org.fastcatsearch.ir.field;

import java.io.IOException;

import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.Output;
import org.fastcatsearch.ir.settings.FieldSetting.Type;

public class DocNoField {
	
	public static int fieldNumber = -3;
	public static String fieldName = Type.__DOCNO.toString();
}