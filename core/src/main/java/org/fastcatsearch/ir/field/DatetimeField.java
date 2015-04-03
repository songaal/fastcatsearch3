package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.Date;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.util.Formatter;

public class DatetimeField extends Field {

	public DatetimeField(String id) {
		super(id, IOUtil.SIZE_OF_LONG);
	}

	public DatetimeField(String id, String data) throws FieldDataParseException {
		super(id, data, IOUtil.SIZE_OF_LONG);
		this.fieldsData = parseData(data);
	}

	@Override
	protected Object parseData(String data) {
		return Formatter.parseDate(data, new Date(0));
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
		if(fieldsData != null){
			output.writeLong(((Date) fieldsData).getTime());
		}else{
			output.writeLong(new Date(0).getTime());
		}
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
			return Formatter.formatDate((Date) fieldsData);
		} else {
			return null;
		}
	}
}
