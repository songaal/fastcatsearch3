package org.fastcatsearch.ir.group.value;

import java.util.List;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class FloatGroupingValue extends GroupingValue<Float> {

    public FloatGroupingValue(GroupFunctionType type) {
        super(type);
    }

	public FloatGroupingValue(Float i, GroupFunctionType type) {
		super(i, type);
	}

	@Override
	public void add(Float i) {
		if (value == null) {
			value = i;
		} else {
			value = value + i;
		}
	}

	@Override
	public void increment() {
        if(value == null) {
            value = 0f;
        }
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Float> o) {
		float cmp = value - o.get();
		return cmp == 0f ? 0 : (cmp < 0 ? -1 : 1);
	}

	public static FloatGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		FloatGroupingValue[] list = new FloatGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new FloatGroupingValue(type);
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}
}
