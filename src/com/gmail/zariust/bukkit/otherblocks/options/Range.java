package com.gmail.zariust.bukkit.otherblocks.options;

public class Range<T extends Number & Comparable<T>> {
	private T min, max;
	
	public Range(T lo, T hi) {
		if(lo == null) {
			if(hi == null) min = max = null;
			else min = max = hi;
		} else if(hi == null) min = max = lo;
		else if(lo.compareTo(hi) == -1) {
			min = lo;
			max = hi;
		} else {
			min = hi;
			max = lo;
		}
	}
	
	public T getMin() {
		return min;
	}
	
	public void setMin(T newMin) {
		min = newMin;
	}
	
	public T getMax() {
		return max;
	}
	
	public void setMax(T newMax) {
		max = newMax;
	}
	
	public boolean contains(T val) {
		if(min == null || max == null || val == null) return true;
		return val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
	}
}
