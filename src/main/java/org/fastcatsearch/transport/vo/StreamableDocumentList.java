package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

	public StreamableDocumentList(List<Document> documentList){
		this.documentList = documentList;
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		Exception ex = null;
		int length = input.readInt();
		this.documentList = new ArrayList<Document>();
		for(int documentInx=0;documentInx<length;documentInx++) {
			int fieldSize = input.readInt();
			int score = input.readInt();
			
			Document document = new Document(fieldSize);
			
			for(int fieldInx=0;fieldInx<fieldSize;fieldInx++) {
				String clsStr = input.readString();
				int len = input.readInt();
				int count = input.readInt();
				int bufLen = input.readInt();
				byte[] buf = new byte[bufLen];
				input.read(buf);
				try {
					Class<?> cls = Class.forName(clsStr);
					Constructor<?>[] constructors = cls.getConstructors();
					for(Constructor<?> constructor : constructors) {
						Class<?>[] parameters = constructor.getParameterTypes();
						
						if(parameters.length > 1 &&
								parameters[0].equals(byte[].class) &&
								parameters[1].equals(int.class)) {
							if(MultiValueField.class.isAssignableFrom(cls) && 
									parameters.length > 3 && 
									parameters[2].equals(int.class) &&
									parameters[3].equals(int.class) ) {
								document.set(fieldInx, (Field) constructor.newInstance
										(buf, len, buf.length, count));
								
							} else {
								if(parameters.length > 2 && parameters[2].equals(int.class)) {
									document.set(fieldInx, (Field) constructor.newInstance
											(buf, len, buf.length));
								
								} else if(parameters.length == 2) {
									document.set(fieldInx, (Field) constructor.newInstance
											(buf, len));
								}
							}
						}
					}
				} catch (ClassNotFoundException e) { ex = e;
				} catch (IllegalArgumentException e) { ex = e;
				} catch (InstantiationException e) { ex = e;
				} catch (IllegalAccessException e) { ex = e;
				} catch (InvocationTargetException e) { ex = e;
				} finally {
					if(ex!=null) {
						throw new IOException(ex);
					}
				}
			}
		}
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
				
				String clsStr = field.getClass().getCanonicalName();
				FastByteBuffer fbb = new FastByteBuffer(field.getRawData().length);
				int len = field.getFixedBytes(fbb);
				int count = 0;
				
				if(field instanceof MultiValueField) {
					count = ((MultiValueField)field).count();
				}
				
				output.writeString(clsStr);
				output.writeInt(len);
				output.writeInt(count);
				output.write(fbb.array.length);
				output.write(fbb.array);
			}
		}
	}
	
	public List<Document> getDocumentList() {
		return documentList;
	}
}
