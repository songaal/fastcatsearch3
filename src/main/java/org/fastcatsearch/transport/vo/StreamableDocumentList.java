package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.document.Document;

public class StreamableDocumentList implements Streamable {

	public StreamableDocumentList(List<Document> documentList){
		
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		// TODO Auto-generated method stub

	}

}
