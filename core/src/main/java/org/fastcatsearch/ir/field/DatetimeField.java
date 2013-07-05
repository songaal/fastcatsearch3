package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;

public class DatetimeField extends Field {
	public static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
	public static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	public static final Pattern ptn = Pattern.compile("[- :.,]");

	public DatetimeField(String id) {
		super(id);
	}

	public DatetimeField(String id, String data) {
		super(id, data);
	}

	@Override
	protected Date parseData(String data) {
		if (data == null) {
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
	public void readFrom(StreamInput input) throws IOException {
		fieldsData = new Date(input.readLong());
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		writeFixedDataTo(output);
	}

	@Override
	public void writeFixedDataTo(StreamOutput output) throws IOException {
		output.writeLong(((Date) fieldsData).getTime());
	}

	@Override
	public void writeDataTo(StreamOutput output) throws IOException {
		writeFixedDataTo(output);
	}

	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		throw new IOException("싱글밸류필드는 writer를 지원하지 않습니다.");
	}

	@Override
	public String toString() {
		if (fieldsData != null) {
			return outputFormat.format((Date) fieldsData);
		} else {
			return null;
		}
	}

}
