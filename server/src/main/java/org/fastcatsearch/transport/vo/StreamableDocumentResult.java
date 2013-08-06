package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.search.DocumentResult;

public class StreamableDocumentResult implements Streamable {

	private DocumentResult documentResult;

	public StreamableDocumentResult() {
	}

	public StreamableDocumentResult(DocumentResult documentResult) {
		this.documentResult = documentResult;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		int size = input.readVInt();
		Row[] rows = new Row[size];
		for (int i = 0; i < rows.length; i++) {
			int fieldCount = input.readVInt();
			rows[i] = new Row(fieldCount);
			for (int j = 0; j < fieldCount; j++) {
				char[] chars =  input.readUString();
				rows[i].put(j, chars);
			}
		}
		
		String[] fieldIdList = new String[input.readVInt()];
		for (int i = 0; i < fieldIdList.length; i++) {
			fieldIdList[i] = input.readString();
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		
		Row[] rows = documentResult.rows();
		output.writeVInt(rows.length);
		for (int i = 0; i < rows.length; i++) {
			Row row = rows[i];
			int fieldCount = row.getFieldCount();
			output.writeVInt(fieldCount);
			for (int j = 0; j < fieldCount; j++) {
				char[] chars = row.get(j);
				output.writeUString(chars, 0, chars.length);
			}
		}
		String[] fieldIdList = documentResult.fieldIdList();
		output.writeVInt(fieldIdList.length);
		for (int i = 0; i < fieldIdList.length; i++) {
			output.writeString(fieldIdList[i]);
		}
	}

	public DocumentResult documentResult() {
		return documentResult;
	}
}
