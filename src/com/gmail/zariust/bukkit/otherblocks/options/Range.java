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
	
	private static String[] split(String range) {
		return range.split("[~-]");
	}
	
	public static Range<Integer> parseIntRange(String range) {
		try {
			String[] split = split(range);
			Integer hi, lo = Integer.valueOf(split[0]);
			if(split.length == 1)
				hi = lo;
			else hi = Integer.valueOf(split[1]);
			return new Range<Integer>(lo, hi);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static Range<Double> parseDoubleRange(String range) {
		try {
			String[] split = split(range);
			Double hi, lo = Double.valueOf(split[0]);
			if(split.length == 1)
				hi = lo;
			else hi = Double.valueOf(split[1]);
			return new Range<Double>(lo, hi);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
