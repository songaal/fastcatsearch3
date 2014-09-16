package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.search.DocumentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableDocumentResult implements Streamable {
	private static Logger logger = LoggerFactory.getLogger(StreamableDocumentResult.class);
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
		
		Row[][] bundleRowList = null;
		int bundleDocsize = input.readVInt();
		if(bundleDocsize > 0) {
			bundleRowList = new Row[bundleDocsize][];
			for (int i = 0; i < bundleDocsize; i++) {
				int rowSize = input.readVInt();
				if(rowSize > 0) {
					bundleRowList[i] = new Row[rowSize];
					
					for (int j = 0; j < rowSize; j++) {
						int fieldCount = input.readVInt();
						bundleRowList[i][j] = new Row(fieldCount);
						for (int k = 0; k < fieldCount; k++) {
							char[] chars =  input.readUString();
							bundleRowList[i][j].put(k, chars);
						}
					}
				}
				
			}
			
		}
		
		String[] fieldIdList = new String[input.readVInt()];
		for (int i = 0; i < fieldIdList.length; i++) {
			fieldIdList[i] = input.readString();
		}
		
		documentResult = new DocumentResult(rows, bundleRowList, fieldIdList);
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
		
		if(documentResult.bundleRows() != null) {
			Row[][] bundleRowList = documentResult.bundleRows();
			output.writeVInt(bundleRowList.length);
			for(Row[] bundleRow : bundleRowList) {
				if(bundleRow != null) {
					output.writeVInt(bundleRow.length);
					for(Row row : bundleRow) {
						int fieldCount = row.getFieldCount();
						output.writeVInt(fieldCount);
						for (int j = 0; j < fieldCount; j++) {
							char[] chars = row.get(j);
							output.writeUString(chars, 0, chars.length);
						}
					}
				} else{
					output.writeVInt(0);		
				}
			}
		}else {
			output.writeVInt(0);
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
