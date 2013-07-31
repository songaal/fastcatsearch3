package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.io.IOUtil;

public class DatetimeField extends Field {
	public static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
	public static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	public static final Pattern ptn = Pattern.compile("[- :.,]");

	public DatetimeField(String id) {
		super(id, IOUtil.SIZE_OF_LONG);
	}

	public DatetimeField(String id, String data) throws FieldDataParseException {
		super(id, data, IOUtil.SIZE_OF_LONG);
	}

	@Override
	protected Date parseData(String data) {
		if (rawString == null) {
			return null;
		}
		
		data = ptn.matcher(data).replaceAll("");
		for (int strlen = data.length(); strlen < 17; strlen++) {
			data += "0";
		}
		try {
			return ((SimpleDateFormat) inputFormat.clone()).parse(data);
		} catch (Exception e) {
			return new Date(0);
		}
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		fieldsData = new Date(input.readLong());
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		writeFixedDataTo(output, 0);
	}

	@Override
	public void writeFixedDataTo(DataOutput output, int indexSize, boolean upperCase) throws IOException {
		output.writeLong(((Date) fieldsData).getTime());
	}

	@Override
	public void writeDataTo(DataOutput output, boolean upperCase) throws IOException {
		writeFixedDataTo(output, 0);
	}

	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		throw new IOException("싱글밸류필드는 writer를 지원하지 않습니다.");
	}

	@Override
	public String getDataString() {
		if (fieldsData != null) {
			return outputFormat.format((Date) fieldsData);
		} else {
			return null;
		}
	}

}
