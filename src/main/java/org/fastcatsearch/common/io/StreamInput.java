package org.fastcatsearch.common.io;

import java.io.IOException;
import java.io.InputStream;

import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.BytesReference;

public abstract class StreamInput extends InputStream {

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
        //TODO softref를 활용한 cache를 이용하여 성능향상필요. 
        char[] chars = new char[len];
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
}
