package org.fastcatsearch.ir.index;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

import java.io.IOException;
import java.util.HashSet;

public class DeleteIdSet extends HashSet<PrimaryKeys> implements Streamable {

    private static final long serialVersionUID = -518125101167212596L;

    private int keySize;

    public DeleteIdSet() {
    }

    public DeleteIdSet(int keySize) {
        this.keySize = keySize;
    }

    public void add(String... keys) throws IRException {
        if (keySize != keys.length) {
            throw new IRException("id field갯수가 일치하지 않습니다.");
        }

        add(new PrimaryKeys(keys));
    }

    public int keySize() {
        return keySize;
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        int size = input.readInt();
        keySize = input.readInt();
        for (int k = 0; k < size; k++) {
            PrimaryKeys keys = new PrimaryKeys(keySize);
            for (int i = 0; i < keySize; i++) {
                keys.set(i, input.readString());
            }
            super.add(keys);
        }
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeInt(this.size());
        output.writeInt(keySize);
        for (PrimaryKeys keys : this) {
            for (int i = 0; i < keySize; i++) {
                String key = keys.getKey(i);
                output.writeString(key);
            }
        }
    }
}
