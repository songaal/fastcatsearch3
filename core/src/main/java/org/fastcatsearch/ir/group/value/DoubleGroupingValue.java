package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class DoubleGroupingValue extends GroupingValue<Double> {

	public DoubleGroupingValue(Double i, GroupFunctionType type) {
        super(i, type);
	}

	@Override
	public void add(Double i) {
		if (value == null) {
			value = (Double) i;
		} else {
			value = (Double) value + (Double) i;
		}
	}

	@Override
	public void increment() {
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Double> o) {
		double cmp = value - (Double) o.get();
		return cmp == 0 ? 0 : (cmp < 0 ? -1 : 1);
	}

	public static DoubleGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		DoubleGroupingValue[] list = new DoubleGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new DoubleGroupingValue(0.0, type);
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

}
