package org.fastcatsearch.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class CollectionIndexData implements Streamable {

	private String collectionId;
	private int documentSize;
	private List<String> fieldList;
	private List<RowData> indexData;

	public CollectionIndexData() {
	}

	public CollectionIndexData(String collectionId, int documentSize, List<String> fieldList, List<RowData> indexData) {
		this.collectionId = collectionId;
		this.documentSize = documentSize;
		this.fieldList = fieldList;
		this.indexData = indexData;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public int getDocumentSize() {
		return documentSize;
	}

	public List<String> getFieldList() {
		return fieldList;
	}

	public List<RowData> getIndexData() {
		return indexData;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		documentSize = input.readVInt();
		int fieldSize = input.readVInt();
		fieldList = new ArrayList<String>(fieldSize);
		for (int i = 0; i < fieldSize; i++) {
			fieldList.add(input.readString());
		}

		int rowSize = input.readVInt();
		indexData = new ArrayList<RowData>(rowSize);
		for (int r = 0; r < rowSize; r++) {
			String segmentId = input.readString();
			String[][] fieldData = new String[fieldSize][];
			for (int i = 0; i < fieldSize; i++) {
				fieldData[i] = new String[] { input.readString(), input.readString() };
			}
			RowData rowData = new RowData(segmentId, fieldData);
			indexData.add(rowData);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeVInt(documentSize);
		if(fieldList != null) {
			output.writeVInt(fieldList.size());
			for(String fieldId : fieldList) {
				output.writeString(fieldId);
			}
		}else{
			output.writeVInt(0);
		}
		
		if(indexData != null) {
			output.writeVInt(indexData.size());
			for(RowData rowData : indexData) {
				output.writeString(rowData.getSegmentId());
				String[][] fieldData = rowData.getFieldData();
				for(int i = 0; i < fieldData.length; i++) {
					output.writeString(fieldData[i][0]);
					output.writeString(fieldData[i][1]);
				}
			}
		}else{
			output.writeVInt(0);			
		}
	}

	public static class RowData {
		private String segmentId;
		private String[][] fieldData;

		public RowData() {
		}

		public RowData(String segmentId, String[][] fieldData) {
			super();
			this.segmentId = segmentId;
			this.fieldData = fieldData;
		}

		public String getSegmentId() {
			return segmentId;
		}

		public String[][] getFieldData() {
			return fieldData;
		}

	}

}
