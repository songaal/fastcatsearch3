package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.config.Field;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.MultiValueField;
import org.fastcatsearch.ir.io.FastByteBuffer;

public class StreamableDocumentList implements Streamable {

	private List<Document> documentList;

	public StreamableDocumentList() {
	}

	public StreamableDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		int length = input.readInt();
		documentList = new ArrayList<Document>();

		for (int documentInx = 0; documentInx < length; documentInx++) {
			int fieldSize = input.readInt();
			float score = input.readFloat();

			Document document = new Document(fieldSize);
			document.setScore(score);
			documentList.add(document);

			for (int fieldInx = 0; fieldInx < fieldSize; fieldInx++) {
				String className = input.readString();
				int fieldByteSize = input.readInt();
				int multiValueCount = input.readInt();
				byte[] buffer = new byte[fieldByteSize];
				input.read(buffer);
				try {
					Class<?> clazz = Class.forName(className);
					Field field = null;

					if (MultiValueField.class.isAssignableFrom(clazz)) {
						// check ACharMVField(byte[] data, int len, int cnt)
						try {
							Constructor<?> constructor = clazz.getConstructor(byte[].class, int.class, int.class);
							field = (Field) constructor.newInstance(buffer, fieldByteSize, multiValueCount);
						} catch (NoSuchMethodException ignore) {
						}
					}

					if (field == null) {
						// check LongField(byte[] data, int len)
						Constructor<?> constructor = clazz.getConstructor(byte[].class, int.class);
						if (constructor != null) {
							field = (Field) constructor.newInstance(buffer, fieldByteSize);
						} else {
							throw new IOException("Cannot find field constructor of class " + className);
						}
					}
					document.set(fieldInx, field);

				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		FastByteBuffer buffer = new FastByteBuffer(8 * 1024); // 재사용버퍼.
		int length = documentList.size();
		output.writeInt(length);
		for (Document document : documentList) {
			int fieldSize = document.size();
			float score = document.getScore();
			output.writeInt(fieldSize);
			output.writeFloat(score);
			for (int fieldInx = 0; fieldInx < fieldSize; fieldInx++) {
				Field field = document.get(fieldInx);

				String className = field.getClass().getCanonicalName();
				buffer.clear();
				int fieldByteSize = field.getFixedBytes(buffer);
				if (fieldByteSize < 0) {
					// 버퍼가 모자르다면 재할당후 데이터를 다시 받는다.
					buffer = new FastByteBuffer(-fieldByteSize);
					fieldByteSize = field.getFixedBytes(buffer);
				}

				int multiValueCount = 0;

				if (field instanceof MultiValueField) {
					multiValueCount = ((MultiValueField) field).count();
				}

				output.writeString(className);
				output.writeInt(fieldByteSize);
				output.writeInt(multiValueCount);
				output.write(buffer.array, 0, fieldByteSize);
			}
		}
	}

	public List<Document> documentList() {
		return documentList;
	}
}
