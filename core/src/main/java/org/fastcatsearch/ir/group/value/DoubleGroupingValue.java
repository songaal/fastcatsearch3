package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupingValue;

public class DoubleGroupingValue extends GroupingValue<Double> {

	public DoubleGroupingValue(Double i) {
		value = i;
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
	public void setIfMax(Double obj) {
		if (value == null) {
			value = obj;
		} else {
			double o = obj;
			double r = (Double) value;
			// 입력값이 기존 값보다 크면 교체한다.
			if (o > r) {
				value = o;
			}
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

	public static DoubleGroupingValue[] createList(int groupKeySize) {
		DoubleGroupingValue[] list = new DoubleGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new DoubleGroupingValue(0.0);
		}
		return list;
	}

}
