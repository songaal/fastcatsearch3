package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class LongGroupingValue extends GroupingValue<Long> {

    public LongGroupingValue(GroupFunctionType type) {
        super(type);
    }

	public LongGroupingValue(Long i, GroupFunctionType type) {
		super(i, type);
	}

	@Override
	public void add(Long i) {
		if (value == null) {
			value = i;
		} else {
			value = value + i;
		}
	}

	@Override
	public void increment() {
        if(value == null) {
            value = 0L;
        }
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Long> o) {
		long cmp = value - o.get();
		return cmp == 0L ? 0 : (cmp < 0L ? -1 : 1);
	}

	public static LongGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		LongGroupingValue[] list = new LongGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new LongGroupingValue(type);
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}
	
}
