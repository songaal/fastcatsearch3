package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class StreamableDocumentList implements Streamable {

	private List<Document> documentList;

	public StreamableDocumentList() {
	}

	public StreamableDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		int length = input.readInt();
		documentList = new ArrayList<Document>();

		for (int documentInx = 0; documentInx < length; documentInx++) {
			int fieldSize = input.readInt();
			int score = input.readInt();

			Document document = new Document(fieldSize);
			document.setScore(score);
			documentList.add(document);

			for (int fieldInx = 0; fieldInx < fieldSize; fieldInx++) {
				String id = input.readString();
				Integer size = input.readVInt();
				String className = input.readString();
				
				try {
					Class<?> clazz = Class.forName(className);
					Field field = null;

					try {
						Constructor<?> constructor = clazz.getConstructor(String.class, int.class);
						field = (Field) constructor.newInstance(id, size);
					} catch (NoSuchMethodException ignore) { }
					field.readRawFrom(input);
					document.add(field);

				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		int length = documentList.size();
		output.writeInt(length);
		for (Document document : documentList) {
			int fieldSize = document.size();
			int score = document.getScore();
			output.writeInt(fieldSize);
			output.writeInt(score);
			for (int fieldInx = 0; fieldInx < fieldSize; fieldInx++) {
				Field field = document.get(fieldInx);
				String id = field.getId();
				int size = field.getSize();
				String className = field.getClass().getCanonicalName();
				
				output.writeString(id);
				output.writeInt(size);
				output.writeString(className);
				
				field.writeRawTo(output);
			}
		}
	}

	public List<Document> documentList() {
		return documentList;
	}
}
