package org.fastcatsearch.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamInput extends InputStream {
	protected static Logger logger = LoggerFactory.getLogger(StreamInput.class);
	public abstract byte readByte() throws IOException;
	
	public abstract void readBytes(byte[] b, int offset, int len) throws IOException;
	
	public void readFully(byte[] b) throws IOException {
        readBytes(b, 0, b.length);
    }
	
	public abstract void reset() throws IOException;
	
	public abstract void close() throws IOException;

	public final boolean readBoolean() throws IOException {
        return readByte() != 0;
    }
	public long readLong() throws IOException {
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}
	public int readInt() throws IOException {
		return (readByte() << 24) + (readByte() << 16) + (readByte() << 8) + (readByte() << 0);
	}
	public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
	public short readShort() throws IOException {
		return (short)((readByte() << 8) + (readByte() << 0));
	}
	public char readUChar() throws IOException {
		return (char)readShort();
	}
	public char readAChar() throws IOException {
		return (char)readByte();
	}
	public int readVInt() throws IOException {
		int v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	
	public long readVLong() throws IOException {
		long v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	public String readString() throws IOException {
        int len = readVInt();
//        logger.debug("read String len = {}", len);
        char[] chars = CachedStreamInput.getCharArray(len);
        int c, charIndex = 0;
        while (charIndex < len) {
            c = readByte() & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[charIndex++] = (char) c;
                    break;
                case 12:
                case 13:
                    chars[charIndex++] = (char) ((c & 0x1F) << 6 | readByte() & 0x3F);
                    break;
                case 14:
                    chars[charIndex++] = (char) ((c & 0x0F) << 12 | (readByte() & 0x3F) << 6 | (readByte() & 0x3F) << 0);
                    break;
            }
        }
        return new String(chars, 0, len);
    }
	
    /**
     * Reads a bytes reference from this stream, might hold an actual reference to the underlying
     * bytes of the stream.
     */
    public BytesReference readBytesReference() throws IOException {
        int length = readVInt();
        return readBytesReference(length);
    }

    /**
     * Reads a bytes reference from this stream, might hold an actual reference to the underlying
     * bytes of the stream.
     */
    public BytesReference readBytesReference(int length) throws IOException {
        if (length == 0) {
            return BytesArray.EMPTY;
        }
        byte[] bytes = new byte[length];
        readBytes(bytes, 0, length);
        return new BytesArray(bytes, 0, length);
    }
    
    public String[] readStringArray() throws IOException {
        int size = readVInt();
        if (size == 0) {
            return Strings.EMPTY_ARRAY;
        }
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = readString();
        }
        return ret;
    }

    public Map<String, Object> readMap() throws IOException {
        return (Map<String, Object>) readGenericValue();
    }
    
    public Object readGenericValue() throws IOException {
        byte type = readByte();
        switch (type) {
            case -1:
                return null;
            case 0:
                return readString();
            case 1:
                return readInt();
            case 2:
                return readLong();
            case 3:
                return readFloat();
            case 4:
                return readDouble();
            case 5:
                return readBoolean();
            case 6:
                int bytesSize = readVInt();
                byte[] value = new byte[bytesSize];
                readBytes(value, 0, bytesSize);
                return value;
            case 7:
                int size = readVInt();
                List list = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    list.add(readGenericValue());
                }
                return list;
            case 8:
                int size8 = readVInt();
                Object[] list8 = new Object[size8];
                for (int i = 0; i < size8; i++) {
                    list8[i] = readGenericValue();
                }
                return list8;
            case 9:
                int size9 = readVInt();
                Map map9 = new LinkedHashMap(size9);
                for (int i = 0; i < size9; i++) {
                    map9.put(readString(), readGenericValue());
                }
                return map9;
            case 10:
                int size10 = readVInt();
                Map map10 = new HashMap(size10);
                for (int i = 0; i < size10; i++) {
                    map10.put(readString(), readGenericValue());
                }
                return map10;
            case 11:
                return readByte();
            case 12:
                return new Date(readLong());
            case 14:
                return readBytesReference();
            case 16:
                return readShort();
            default:
                throw new IOException("Can't read unknown type [" + type + "]");
        }
    }
}
