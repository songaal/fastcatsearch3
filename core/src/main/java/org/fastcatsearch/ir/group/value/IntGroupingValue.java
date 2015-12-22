package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class IntGroupingValue extends GroupingValue<Integer> {

    public IntGroupingValue(GroupFunctionType type) {
        super(type);
    }

	public IntGroupingValue(Integer i, GroupFunctionType type) {
        super(i, type);
    }

	@Override
	public void add(Integer i) {
        if(i != null) {
            if (value == null) {
                value = i;
            } else {
                value += i;
            }
        }
	}

	@Override
	public void increment() {
        if(value == null) {
            value = 0;
        }
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Integer> o) {
		return value - o.get();
	}

	public static IntGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		IntGroupingValue[] list = new IntGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new IntGroupingValue(type);
		}
		return list;
	}
	
	@Override
	public boolean isEmpty() {
        return value == null || value == 0;
    }

}
