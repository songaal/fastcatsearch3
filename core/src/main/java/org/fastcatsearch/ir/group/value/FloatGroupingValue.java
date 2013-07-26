package org.fastcatsearch.ir.group.value;

import java.util.List;

import org.fastcatsearch.ir.group.GroupingValue;

public class FloatGroupingValue extends GroupingValue<Float> {

	public FloatGroupingValue(Float i) {
		value = i;
	}

	@Override
	public void add(Float i) {
		if (value == null) {
			value = (Float) i;
		} else {
			value = (Float) value + (Float) i;
		}
	}

	@Override
	public void setIfMax(Float obj) {
		if (value == null) {
			value = obj;
		} else {
			float o = obj;
			float r = (Float) value;
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
	public int compareTo(GroupingValue<Float> o) {
		float cmp = value - (Float) o.get();
		return cmp == 0f ? 0 : (cmp < 0 ? -1 : 1);
	}

	public static FloatGroupingValue[] createList(int groupKeySize) {
		FloatGroupingValue[] list = new FloatGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new FloatGroupingValue(0.0f);
		}
		return list;
	}

}
