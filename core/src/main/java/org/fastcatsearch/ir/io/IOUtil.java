/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public final class IOUtil {
	public final static int PAGESIZE = 1 * 1024;
	public final static int FILEBLOCKSIZE = 4 * 1024;
	public final static int SIZE_OF_SHORT = Short.SIZE / 8;
	public final static int SIZE_OF_INT = Integer.SIZE / 8;
	public final static int SIZE_OF_LONG = Long.SIZE / 8;
	public final static int BITS_OF_BYTE = Byte.SIZE;
	public final static int BITS_OF_LONG = Long.SIZE;

	public static void writeLong(BytesBuffer buffer, long i) {
		// int i1 = (int)((v >>> 32) & 0xFFFFFFFF);
		// int i2 = (int)(v & 0xFFFFFFFF);
		// writeInt(buffer,i1);
		// writeInt(buffer,i2);
		writeInt(buffer, (int) (i >> 32));
		writeInt(buffer, (int) i);
	}

	public static void writeInt(BytesBuffer buffer, int i) {
		// buffer.write((v >>> 24) & 0xFF);
		// buffer.write((v >>> 16) & 0xFF);
		// buffer.write((v >>> 8) & 0xFF);
		// buffer.write((v >>> 0) & 0xFF);
		buffer.write((byte) (i >> 24));
		buffer.write((byte) (i >> 16));
		buffer.write((byte) (i >> 8));
		buffer.write((byte) i);
	}

	public static void writeShort(BytesBuffer buffer, int i) {
		// buffer.write((v >>> 8) & 0xFF);
		// buffer.write((v >>> 0) & 0xFF);
		buffer.write((byte) (i >> 8));
		buffer.write((byte) i);
	}

	public static void writeUChar(BytesBuffer buffer, int i) {
		writeShort(buffer, i);
	}

	public static void writeAChar(BytesBuffer buffer, int i) {
		buffer.write((byte) i);
	}

	public static void writeVInt(BytesBuffer buffer, int i) {
		while ((i & ~0x7F) != 0) {
			buffer.write((byte) ((i & 0x7F) | 0x80));
			i >>>= 7;
		}
		buffer.write((byte) i);
	}

	public static void writeVInt(OutputStream os, int i) throws IOException {
		while ((i & ~0x7F) != 0) {
			os.write(((i & 0x7F) | 0x80));
			i >>>= 7;
		}
		os.write(i);
	}

	// 입력정수의 가변길이를 계산한다.
	public static int lenVariableByte(int v) {
		int byteCnt = 1;
		// 7바이트씩 몇번 shift를 하게되는지 계산하면 된다. 부호비트를 유지하지 않는 logical shift를 이용한다.
		while ((v >>>= 7) != 0)
			byteCnt++;

		return byteCnt;
	}

	// -----------------------------------------
	// READ
	// -----------------------------------------

	public static long readLong(byte[] buffer, int pos) {
		// return ((buffer[pos] & 0xff) << 56) + ((buffer[pos + 1] & 0xff) << 48) + ((buffer[pos + 2] & 0xff) << 40) +
		// ((buffer[pos + 3] & 0xff) << 32)
		// + ((buffer[pos + 4] & 0xff) << 24) + ((buffer[pos + 5] & 0xff) << 16) + ((buffer[pos + 6] & 0xff) << 8) + ((buffer[pos
		// + 7] & 0xff) << 0);
		return (((long) readInt(buffer, pos)) << 32) | (readInt(buffer, pos+4) & 0xFFFFFFFFL);
	}

	public static int readInt(BytesBuffer buffer) {
		// return (buffer.read() << 24) + (buffer.read() << 16) + (buffer.read() << 8) + (buffer.read() << 0);
		return ((buffer.readByte() & 0xFF) << 24) | ((buffer.readByte() & 0xFF) << 16) | ((buffer.readByte() & 0xFF) << 8)
				| (buffer.readByte() & 0xFF);
	}

	public static int readInt(byte[] buffer, int pos) {
		// return ((buffer[pos + 0] & 0xff) << 24) + ((buffer[pos + 1] & 0xff) << 16) + ((buffer[pos + 2] & 0xff) << 8) +
		// ((buffer[pos + 3] & 0xff) << 0);
		return ((buffer[pos + 0] & 0xFF) << 24) | ((buffer[pos + 1] & 0xFF) << 16) | ((buffer[pos + 2] & 0xFF) << 8) | (buffer[pos + 3] & 0xFF);
	}

	public static short readShort(byte[] buffer, int pos) {
		return (short) (((buffer[pos] & 0xFF) << 8) | (buffer[pos + 1] & 0xFF));
	}

	public static int readVInt(BytesBuffer buffer) {

		byte b = buffer.readByte();
		if (b >= 0)
			return b;
		int i = b & 0x7F;
		b = buffer.readByte();
		i |= (b & 0x7F) << 7;
		if (b >= 0)
			return i;
		b = buffer.readByte();
		i |= (b & 0x7F) << 14;
		if (b >= 0)
			return i;
		b = buffer.readByte();
		i |= (b & 0x7F) << 21;
		if (b >= 0)
			return i;
		b = buffer.readByte();
		// Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
		i |= (b & 0x0F) << 28;
		if ((b & 0xF0) == 0)
			return i;

		return -1;
	}

	public static int readVInt(byte[] buffer, int pos) {

		byte b = buffer[pos++];
		if (b >= 0)
			return b;
		int i = b & 0x7F;
		b = buffer[pos++];
		i |= (b & 0x7F) << 7;
		if (b >= 0)
			return i;
		b = buffer[pos++];
		i |= (b & 0x7F) << 14;
		if (b >= 0)
			return i;
		b = buffer[pos++];
		i |= (b & 0x7F) << 21;
		if (b >= 0)
			return i;
		b = buffer[pos++];
		// Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
		i |= (b & 0x0F) << 28;
		if ((b & 0xF0) == 0)
			return i;

		return -1;
	}

	public static char[] readAChars(byte[] buffer, int pos, int len) {
		char[] array = new char[len];
		for (int i = 0; i < len; i++)
			array[i] = (char) buffer[pos + i];

		return array;
	}

	public static char[] readUChars(byte[] buffer, int pos, int len) {
		char[] array = new char[len / 2];
		for (int i = 0; i < array.length; i++) {
			array[i] = (char) readShort(buffer, pos + i * 2);
		}

		return array;
	}

	public static boolean copy(File a, File b) throws IOException {
		FileChannel fc1 = new FileInputStream(a).getChannel();
		if (b.exists())
			b.delete();
		FileChannel fc2 = new FileOutputStream(b).getChannel();

		long count1 = fc1.size();
		fc1.transferTo(0, count1, fc2);

		fc1.close();
		fc2.close();
		return true;
	}

	public static void transferFrom(DataOutput output, File inFile, long offset, long length, byte[] buffer) throws IOException {
		RandomAccessFile rin = null;
		try {
			rin = new RandomAccessFile(inFile, "r");
			rin.seek(offset);
			int size = Math.min((int) length, buffer.length);// 대부분 buffer사이즈가 되며, length가 작을 경우에만, length가 된다.
			while (length > 0) {
				int nread = rin.read(buffer, 0, size);
				output.writeBytes(buffer, 0, nread);
				length -= nread;
			}
		} finally {
			if (rin != null) {
				rin.close();
			}
		}
	}

	public static void transferFrom(DataOutput output, DataInput input, long length, byte[] buffer) throws IOException {
		while (length > 0) {
			int size = Math.min((int) length, buffer.length);
			input.readBytes(buffer, 0, size);
			output.writeBytes(buffer, 0, size);
			length -= size;
		}
	}


    public static byte readByte(RandomAccessFile raf) throws IOException {
        return raf.readByte();
    }

    public static int readInt(RandomAccessFile raf) throws IOException {
        return ((readByte(raf) & 0xFF) << 24) | ((readByte(raf) & 0xFF) << 16) | ((readByte(raf) & 0xFF) << 8) | (readByte(raf) & 0xFF);
    }

    public static long readLong(RandomAccessFile raf) throws IOException {
        return (((long) readInt(raf)) << 32) | (readInt(raf) & 0xFFFFFFFFL);
    }
}
