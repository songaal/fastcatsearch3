package org.apache.lucene.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Comparator;

import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;

/**
 * Represents byte[], as a slice (offset + length) into an existing byte[]. The {@link #bytes} member should never be null; use
 * {@link #EMPTY_BYTES} if necessary.
 *
 * <p>
 * <b>Important note:</b> Unless otherwise noted, Lucene uses this class to represent terms that are encoded as <b>UTF8</b> bytes
 * in the index. To convert them to a Java {@link String} (which is UTF16), use {@link #utf8ToString}. Using code like
 * {@code new String(bytes, offset, length)} to do this is <b>wrong</b>, as it does not respect the correct character set and may
 * return wrong results (depending on the platform's defaults)!
 */
public final class BytesRef extends BytesBuffer implements Comparable<BytesRef>, Cloneable  {
    /** An empty byte array for convenience */
    public static final byte[] EMPTY_BYTES = new byte[0];

    /** Length of used bytes. */
    public int length;

    /** Create a BytesRef with {@link #EMPTY_BYTES} */
    public BytesRef() {
        this(EMPTY_BYTES);
    }

    /**
     * This instance will directly reference bytes w/o making a copy. bytes should not be null.
     */
    public BytesRef(byte[] bytes, int offset, int length) {
        super(bytes, offset, offset + length);
        this.length = length;
        assert isValid();
    }

    /**
     * This instance will directly reference bytes w/o making a copy. bytes should not be null
     */
    public BytesRef(byte[] bytes) {
        super(bytes, 0, bytes.length);
        this.length = bytes.length;
    }

    /**
     * Create a BytesRef pointing to a new array of size <code>capacity</code>. Offset and length will both be zero.
     */
    public BytesRef(int capacity) {
        this(new byte[capacity]);
//		this.bytes = new byte[capacity];
    }

    /**
     * Initialize the byte[] from the UTF8 bytes for the provided String.
     *
     * @param text
     *            This must be well-formed unicode text, with no unpaired surrogates.
     */
    public BytesRef(CharSequence text) {
        this();
        copyChars(text);
    }

    public int length() {
        return length;
    }

    public void reset(){
        offset = 0;
        length = bytes.length;
    }
    /**
     * Copies the UTF8 bytes for this string.
     *
     * @param text
     *            Must be well-formed unicode text, with no unpaired surrogates or invalid UTF16 code units.
     */
    public void copyChars(CharSequence text) {
        assert offset == 0; // TODO broken if offset != 0
        UnicodeUtil.UTF16toUTF8(text, 0, text.length(), this);
    }

    /**
     * Expert: compares the bytes against another BytesRef, returning true if the bytes are equal.
     *
     * @param other
     *            Another BytesRef, should not be null.
     * @lucene.internal
     */
    public boolean bytesEquals(BytesRef other) {
        assert other != null;
        if (length == other.length) {
            int otherUpto = other.offset;
            final byte[] otherBytes = other.bytes;
            final int end = offset + length;
            for (int upto = offset; upto < end; upto++, otherUpto++) {
                if (bytes[upto] != otherBytes[otherUpto]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean bytesEquals(BytesRef other, int length) {
        assert other != null;
        // if (length == other.length) {
        int otherUpto = other.offset;
        final byte[] otherBytes = other.bytes;
        final int end = offset + length;
        for (int upto = offset; upto < end; upto++, otherUpto++) {
            if (bytes[upto] != otherBytes[otherUpto]) {
                return false;
            }
        }
        return true;
        // } else {
        // return false;
        // }
    }

    @Override
    public BytesRef clone() {
        return new BytesRef(bytes, offset, length);
    }

    /**
     * Calculates the hash code as required by TermsHash during indexing.
     * <p>
     * It is defined as:
     *
     * <pre class="prettyprint">
     * int hash = 0;
     * for (int i = offset; i &lt; offset + length; i++) {
     * 	hash = 31 * hash + bytes[i];
     * }
     * </pre>
     */
    @Override
    public int hashCode() {
        int hash = 0;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            hash = 31 * hash + bytes[i];
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof BytesRef) {
            return this.bytesEquals((BytesRef) other);
        }
        return false;
    }

    public int toIntValue(){
        return IOUtil.readInt(bytes, offset);
    }
    public long toLongValue(){
        return IOUtil.readLong(bytes, offset);
    }
    public char[] toACharArray(){
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = (char) bytes[offset + i];
        }
        return result;
    }
    public char[] toUCharArray(){
        char[] result = new char[length / 2];
        int pos = 0;
        for (int i = 0; i < length; i+=2) {
            result[pos++] = (char) IOUtil.readShort(bytes, i);
        }

        return result;
    }

    public String toStringValue() {
        return new String(bytes, offset, length);
    }

    /** Returns hex encoded bytes, eg [0x6c 0x75 0x63 0x65 0x6e 0x65] */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (i > offset) {
                sb.append(' ');
            }
            sb.append(Integer.toHexString(bytes[i] & 0xff));
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Copies the bytes from the given {@link BytesRef}
     * <p>
     * NOTE: if this would exceed the array size, this method creates a new reference array.
     */
    public void copyBytes(BytesRef other) {
        if (bytes.length - offset < other.length) {
            bytes = new byte[other.length];
            offset = 0;
        }
        System.arraycopy(other.bytes, other.offset, bytes, offset, other.length);
        length = other.length;
    }

    public BytesRef duplicate() {
        byte[] bytes = new byte[length];
        System.arraycopy(this.bytes, this.offset, bytes, 0, length);
        return new BytesRef(bytes, 0, length);
    }


    /**
     * Appends the bytes from the given {@link BytesRef}
     * <p>
     * NOTE: if this would exceed the array size, this method creates a new reference array.
     */
    public void append(BytesRef other) {
        int newLen = length + other.length;
        if (bytes.length - offset < newLen) {
            byte[] newBytes = new byte[newLen];
            System.arraycopy(bytes, offset, newBytes, 0, length);
            offset = 0;
            bytes = newBytes;
        }
        System.arraycopy(other.bytes, other.offset, bytes, length + offset, other.length);
        length = newLen;
    }

    public void append(byte[] other, int off, int len) {
        int newLen = this.length + length;
        if (bytes.length - offset < newLen) {
            byte[] newBytes = new byte[newLen];
            System.arraycopy(bytes, offset, newBytes, 0, length);
            offset = 0;
            bytes = newBytes;
        }
        System.arraycopy(other, off, bytes, length + offset, len);
        length = newLen;
    }

    /**
     * Used to grow the reference array.
     *
     * In general this should not be used as it does not take the offset into account.
     *
     * @lucene.internal
     */
    public void grow(int newLength) {
        assert offset == 0; // NOTE: senseless if offset != 0
        bytes = ArrayUtil.grow(bytes, newLength);
    }

    /**
     * Creates a new BytesRef that points to a copy of the bytes from <code>other</code>
     * <p>
     * The returned BytesRef will have a length of other.length and an offset of zero.
     */
    public static BytesRef deepCopyOf(BytesRef other) {
        BytesRef copy = new BytesRef();
        copy.copyBytes(other);
        return copy;
    }

    /**
     * Performs internal consistency checks. Always returns true (or throws IllegalStateException)
     */
    public boolean isValid() {
        if (bytes == null) {
            throw new IllegalStateException("bytes is null");
        }
        if (length < 0) {
            throw new IllegalStateException("length is negative: " + length);
        }
        if (length > bytes.length) {
            throw new IllegalStateException("length is out of bounds: " + length + ",bytes.length=" + bytes.length);
        }
        if (offset < 0) {
            throw new IllegalStateException("offset is negative: " + offset);
        }
        if (offset > bytes.length) {
            throw new IllegalStateException("offset out of bounds: " + offset + ",bytes.length=" + bytes.length);
        }
        if (offset + length < 0) {
            throw new IllegalStateException("offset+length is negative: offset=" + offset + ",length=" + length);
        }
        if (offset + length > bytes.length) {
            throw new IllegalStateException("offset+length out of bounds: offset=" + offset + ",length=" + length
                    + ",bytes.length=" + bytes.length);
        }
        return true;
    }

    @Override
    public int compareTo(BytesRef other) {
        if(bytes == null && (other == null || other.bytes == null))
            return 0;
        else if(bytes == null)
            return -1;
        else if(other == null || other.bytes == null)
            return 1;

        int l = (length < other.length) ? length : other.length;
        if(l == 0){
            return length - other.length;
        }
        //오름차순.
        for (int i = 0; i < l; i++) {
            if(bytes[offset + i] != other.bytes[other.offset + i]){
                return (bytes[offset + i] & 0xff) - (other.bytes[other.offset + i] & 0xff);
            }
        }

        return length - other.length;
    }

    public int compareNumberTo(BytesRef other) {
        if(bytes == null && (other == null || other.bytes == null))
            return 0;
        else if(bytes == null)
            return -1;
        else if(other == null || other.bytes == null)
            return 1;

        int l = (length < other.length) ? length : other.length;
        if(l == 0){
            return length - other.length;
        }
        //오름차순.
        if(bytes[offset] >= 0 && other.bytes[other.offset] < 0){
            return 1;
        }else if(bytes[offset] < 0 && other.bytes[other.offset] >= 0){
            return -1;
        }else{
            //부호가 같은 경우
            for (int i = 0; i < l; i++) {
                if(bytes[offset + i] != other.bytes[other.offset + i]){
                    return (bytes[offset + i] & 0xff) - (other.bytes[other.offset + i] & 0xff);
                }
            }
        }

        return length - other.length;
    }

    public int remaining() {
        return length - offset;
    }


}
