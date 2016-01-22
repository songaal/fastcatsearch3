package org.fastcatsearch.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class CollectionAnalyzedIndexData extends CollectionIndexData {
	private List<RowData> pkData;
	private List<RowData> analyzedData;
	
	public CollectionAnalyzedIndexData() {}
	
	public CollectionAnalyzedIndexData(String collectionId, int documentSize, int deleteSize, List<String> fieldList, List<RowData> pkData, List<RowData> indexData, List<RowData> analyzedData, List<Boolean> isDeletedList) {
		super(collectionId, documentSize, deleteSize, fieldList, indexData, isDeletedList);
		this.pkData = pkData;
		this.analyzedData = analyzedData;
	}

	public List<RowData> getPkData() {
		return pkData;
	}

	public void setPkData(List<RowData> pkData) {
		this.pkData = pkData;
	}

	public List<RowData> getAnalyzedData() {
		return analyzedData;
	}

	public void setAnalyzedData(List<RowData> analyzedData) {
		this.analyzedData = analyzedData;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		
		int fieldSize = fieldList.size();
		
		int pkRowSize = input.readVInt();
		if(pkRowSize > 0) {
			int pkFieldSize = input.readVInt();
			pkData = new ArrayList<RowData>(pkRowSize); 
			for (int r = 0; r < pkRowSize; r++) {
				String[][] data = new String[pkFieldSize][];
				for (int i = 0; i < pkFieldSize; i++) {
					data[i] = new String[] { input.readString(), input.readString() };
				}
				RowData rowData = new RowData("", data);
				pkData.add(rowData);
			}
		}
		
		int analyzedRowSize = input.readVInt();
		if(analyzedRowSize > 0) {
			analyzedData = new ArrayList<RowData>(analyzedRowSize);
			for (int r = 0; r < analyzedRowSize; r++) {
				String[][] fieldData = new String[fieldSize][];
				for (int i = 0; i < fieldSize; i++) {
					fieldData[i] = new String[] { input.readString(), input.readString() };
				}
				RowData rowData = new RowData("", fieldData);
				analyzedData.add(rowData);
			}
		}
		
	}
	
	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		if(pkData != null && pkData.size() > 0) {
			output.writeVInt(pkData.size());
			int pkFieldSize = pkData.get(0).getFieldData().length;
			output.writeVInt(pkFieldSize);
			for(RowData rowData : pkData) {
				String[][] fieldData = rowData.getFieldData();
				for(int i = 0; i < fieldData.length; i++) {
					output.writeString(fieldData[i][0]);
					output.writeString(fieldData[i][1]);
				}
			}
		}else{
			output.writeVInt(0);
		}
		
		if(analyzedData != null && analyzedData.size() > 0) {
			output.writeVInt(analyzedData.size());
			for(RowData rowData : analyzedData) {
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
}
