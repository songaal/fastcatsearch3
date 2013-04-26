package org.fastcatsearch.transport.vo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.ir.config.Field;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.DoubleMVField;
import org.fastcatsearch.ir.field.IntField;
import org.fastcatsearch.ir.field.UCharField;
import org.fastcatsearch.ir.field.UCharMVField;
import org.fastcatsearch.ir.io.FastByteBuffer;
import org.junit.Assert;
import org.junit.Test;

public class StreamableDocumentListTest {

	@Test
	public void testWriteAndRead() throws IOException {
		List<Document> documentList = new ArrayList<Document>();
		Document document1 = new Document(4);
		document1.add(new IntField("1"));
		document1.add(new DoubleMVField("10\t11\t12\t13", '\t', Short.MAX_VALUE));
		document1.add(new UCharField("안녕하세요."));
		document1.add(new UCharMVField("안녕하세요.\t수고하세요\t감사합니다.\t사랑합니다.", -1, '\t', Short.MAX_VALUE));
		documentList.add(document1);
		
		Document document2 = new Document(4);
		document2.add(new IntField("2"));
		document2.add(new DoubleMVField("20\t21\t22\t23", '\t', Short.MAX_VALUE));
		document2.add(new UCharField("패스트캣서치"));
		document2.add(new UCharMVField("찾아주세요.\t부탁입니다.\t빨리요.\t찾으셨군요.", -1, '\t', Short.MAX_VALUE));
		documentList.add(document2);
		
		
		List<Document> expected = documentList;
		StreamableDocumentList StreamableDocumentList1 = new StreamableDocumentList(documentList);
		BytesStreamOutput output = new BytesStreamOutput();
		StreamableDocumentList1.writeTo(output);
		
		BytesReference ref = output.bytes();
		StreamInput input = new BytesStreamInput(ref);
		
		StreamableDocumentList StreamableDocumentList2 = new StreamableDocumentList();
		StreamableDocumentList2.readFrom(input);
		List<Document> actual = StreamableDocumentList2.documentList();
		
		assertTrue(expected.size() == actual.size());
		
		FastByteBuffer buffer1 = new FastByteBuffer(128);
		FastByteBuffer buffer2 = new FastByteBuffer(128);
		for (int i = 0; i < expected.size(); i++) {
			Document expectedDocument = expected.get(i);
			Document actualDocument = actual.get(i);
			assertTrue(expectedDocument.size() == actualDocument.size());
			
			for (int j = 0; j < expectedDocument.size(); j++) {
				
				Field expectedField = expectedDocument.get(j);
				Field actualField = actualDocument.get(j);
				
				buffer1.clear();
				buffer2.clear();
				
				expectedField.getFixedBytes(buffer1);
				actualField.getFixedBytes(buffer2);
				
				buffer1.flip();
				buffer2.flip();
				
				Assert.assertEquals(buffer1.remaining(), buffer2.remaining());
				assertTrue(buffer1.equals(buffer2));
				
			}
			
		}
	}

}
