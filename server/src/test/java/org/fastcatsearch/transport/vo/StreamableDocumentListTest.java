package org.fastcatsearch.transport.vo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.DoubleField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.field.IntField;
import org.fastcatsearch.ir.field.UStringField;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.junit.Assert;
import org.junit.Test;

public class StreamableDocumentListTest {

	@Test
	public void testWriteAndRead() throws IOException, FieldDataParseException {
		List<Document> documentList = new ArrayList<Document>();
		Document document1 = new Document(4);
		document1.add(new IntField("f0", "1"));
		document1.add(new DoubleField("f1", "111.0"));
		document1.add(new UStringField("f2", "안녕하세요."));
		document1.add(new UStringField("f3", "안녕하세요.\t수고하세요\t감사합니다.\t사랑합니다."));
		documentList.add(document1);
		
		Document document2 = new Document(4);
		document2.add(new IntField("f0", "2"));
		document2.add(new DoubleField("f1", "111.0"));
		document2.add(new UStringField("f2", "안녕하세요."));
		document2.add(new UStringField("f3", "안녕하세요.\t수고하세요\t감사합니다.\t사랑합니다."));
		documentList.add(document2);
		
		
		List<Document> expected = documentList;
		StreamableDocumentList StreamableDocumentList1 = new StreamableDocumentList(documentList);
		BytesStreamOutput output = new BytesStreamOutput();
		StreamableDocumentList1.writeTo(output);
		
		BytesReference ref = output.bytesReference();
		StreamInput input = new BytesStreamInput(ref);
		
		StreamableDocumentList StreamableDocumentList2 = new StreamableDocumentList();
		StreamableDocumentList2.readFrom(input);
		List<Document> actual = StreamableDocumentList2.documentList();
		
		assertTrue(expected.size() == actual.size());
		
		BytesDataOutput buffer1 = new BytesDataOutput(128);
		BytesDataOutput buffer2 = new BytesDataOutput(128);
		for (int i = 0; i < expected.size(); i++) {
			Document expectedDocument = expected.get(i);
			Document actualDocument = actual.get(i);
			assertTrue(expectedDocument.size() == actualDocument.size());
			
			for (int j = 0; j < expectedDocument.size(); j++) {
				
				Field expectedField = expectedDocument.get(j);
				Field actualField = actualDocument.get(j);
				
				buffer1.reset();
				buffer2.reset();
				
				expectedField.writeTo(buffer1);
				actualField.writeTo(buffer2);
				
				assertTrue(buffer1.bytesRef().equals(buffer2.bytesRef()));
				
			}
			
		}
	}

}
