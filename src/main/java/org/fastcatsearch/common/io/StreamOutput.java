package org.fastcatsearch.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.BytesReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamOutput extends OutputStream{
	protected static Logger logger = LoggerFactory.getLogger(StreamOutput.class);
	
	public boolean seekPositionSupported() {
        return false;
    }

    public long position() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void seek(long position) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public abstract void writeByte(int b) throws IOException;
    
    public void writeBytes(byte[] b) throws IOException {
        writeBytes(b, 0, b.length);
    }
    
    public void writeBytes(byte[] b, int length) throws IOException {
        writeBytes(b, 0, length);
    }
    
    public abstract void writeBytes(byte[] b, int offset, int length) throws IOException;

    public abstract void flush() throws IOException;
    
    public abstract void close() throws IOException;
    
    public abstract void reset() throws IOException;
    
    @Override
    public void write(int b) throws IOException {
        writeByte(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeBytes(b, off, len);
    }
    
    
    public void writeBoolean(boolean b) throws IOException {
        writeByte(b ? 1 : 0);
    }
    public void writeLong(long v) throws IOException {
		int i1 = (int)((v >>> 32) & 0xFFFFFFFF);
		int i2 = (int)(v & 0xFFFFFFFF);
		writeInt(i1);
		writeInt(i2);
	}
	public void writeInt(int v) throws IOException {
		writeByte((v >>> 24) & 0xFF);
		writeByte((v >>> 16) & 0xFF);
		writeByte((v >>> 8) & 0xFF);
		writeByte((v >>> 0) & 0xFF);
	}
	public void writeShort(int v) throws IOException {
		writeByte((v >>> 8) & 0xFF);
		writeByte((v >>> 0) & 0xFF);
	}
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }
    
    public int writeVInt(int v) throws IOException {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				writeByte(b);
			}else{
				writeByte(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
    public int writeVLong(long v) throws IOException {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				writeByte(b);
			}else{
				writeByte(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
    public void writeString(String str) throws IOException {
        int charCount = str.length();
//        logger.debug("writeString len = {}", charCount);
        writeVInt(charCount);
        int c;
        for (int i = 0; i < charCount; i++) {
            c = str.charAt(i);
            if (c <= 0x007F) {
                writeByte((byte) c);
            } else if (c > 0x07FF) {
                writeByte((byte) (0xE0 | c >> 12 & 0x0F));
                writeByte((byte) (0x80 | c >> 6 & 0x3F));
                writeByte((byte) (0x80 | c >> 0 & 0x3F));
            } else {
                writeByte((byte) (0xC0 | c >> 6 & 0x1F));
                writeByte((byte) (0x80 | c >> 0 & 0x3F));
            }
        }
    }
    
    
    public void writeBytesReference(BytesReference bytes) throws IOException {
        if (bytes == null) {
            writeVInt(0);
            return;
        }
        writeVInt(bytes.length());
        bytes.writeTo(this);
    }
    
    public void writeStringArray(String[] array) throws IOException {
        writeVInt(array.length);
        for (String s : array) {
            writeString(s);
        }
    }

    /**
     * Writes a string array, for nullable string, writes it as 0 (empty string).
     */
    public void writeStringArrayNullable(String[] array) throws IOException {
        if (array == null) {
            writeVInt(0);
        } else {
            writeVInt(array.length);
            for (String s : array) {
                writeString(s);
            }
        }
    }

    public void writeMap(Map<String, Object> map) throws IOException {
        writeGenericValue(map);
    }

    public void writeGenericValue(Object value) throws IOException {
        if (value == null) {
            writeByte((byte) -1);
            return;
        }
        Class type = value.getClass();
        if (type == String.class) {
            writeByte((byte) 0);
            writeString((String) value);
        } else if (type == Integer.class) {
            writeByte((byte) 1);
            writeInt((Integer) value);
        } else if (type == Long.class) {
            writeByte((byte) 2);
            writeLong((Long) value);
        } else if (type == Float.class) {
            writeByte((byte) 3);
            writeFloat((Float) value);
        } else if (type == Double.class) {
            writeByte((byte) 4);
            writeDouble((Double) value);
        } else if (type == Boolean.class) {
            writeByte((byte) 5);
            writeBoolean((Boolean) value);
        } else if (type == byte[].class) {
            writeByte((byte) 6);
            writeVInt(((byte[]) value).length);
            writeBytes(((byte[]) value));
        } else if (value instanceof List) {
            writeByte((byte) 7);
            List list = (List) value;
            writeVInt(list.size());
            for (Object o : list) {
                writeGenericValue(o);
            }
        } else if (value instanceof Object[]) {
            writeByte((byte) 8);
            Object[] list = (Object[]) value;
            writeVInt(list.length);
            for (Object o : list) {
                writeGenericValue(o);
            }
        } else if (value instanceof Map) {
            if (value instanceof LinkedHashMap) {
                writeByte((byte) 9);
            } else {
                writeByte((byte) 10);
            }
            Map<String, Object> map = (Map<String, Object>) value;
            writeVInt(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                writeString(entry.getKey());
                writeGenericValue(entry.getValue());
            }
        } else if (type == Byte.class) {
            writeByte((byte) 11);
            writeByte((Byte) value);
        } else if (type == Date.class) {
            writeByte((byte) 12);
            writeLong(((Date) value).getTime());
        } else if (value instanceof BytesReference) {
            writeByte((byte) 14);
            writeBytesReference((BytesReference) value);
        } else if (type == Short.class) {
            writeByte((byte) 16);
            writeShort((Short) value);
        } else {
            throw new IOException("Can't write type [" + type + "]");
        }
    }
    
}
