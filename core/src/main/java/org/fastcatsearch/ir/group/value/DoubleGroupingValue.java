package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class DoubleGroupingValue extends GroupingValue<Double> {

    public DoubleGroupingValue(GroupFunctionType type) {
        super(type);
    }

	public DoubleGroupingValue(Double i, GroupFunctionType type) {
        super(i, type);
	}

	@Override
	public void add(Double i) {
		if (value == null) {
			value = i;
		} else {
			value = value + i;
		}
	}

	@Override
	public void increment() {
        if(value == null) {
            value = 0d;
        }
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Double> o) {
		double cmp = value - o.get();
		return cmp == 0 ? 0 : (cmp < 0 ? -1 : 1);
	}

	public static DoubleGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		DoubleGroupingValue[] list = new DoubleGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new DoubleGroupingValue(type);
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

}
