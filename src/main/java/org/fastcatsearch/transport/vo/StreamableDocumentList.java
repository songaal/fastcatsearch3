package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.config.Field;
import org.fastcatsearch.ir.document.Document;

public class StreamableDocumentList implements Streamable {
	
	private List<Document> documentList;

	public StreamableDocumentList(List<Document> documentList){
		this.documentList = documentList;
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		int length = documentList.size();
		output.writeInt(length);
		for(int documentInx=0;documentInx<length;documentInx++) {
			Document document = documentList.get(documentInx);
			int fieldSize = document.size();
			int score = document.getScore();
			output.writeInt(fieldSize);
			output.writeInt(score);
			for(int fieldInx=0;fieldInx<fieldSize;fieldInx++) {
				Field field = document.get(fieldInx);
			}
		}
		// TODO Auto-generated method stub

	}
	
	public List<Document> getDocumentList() {
		return documentList;
	}
}
