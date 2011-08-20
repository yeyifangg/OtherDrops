package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.Random;

public abstract class Range<T extends Number & Comparable<T>> {
	protected T min, max;
	
	public Range(T val) {
		min = max = val;
	}
	
	public Range(T lo, T hi) {
		if(lo == null) {
			if(hi == null) min = max = null;
			else min = max = hi;
		} else if(hi == null) min = max = lo;
		else if(lo.compareTo(hi) < 0) {
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
		if(min.compareTo(max) > 0) max = min;
	}
	
	public T getMax() {
		return max;
	}
	
	public void setMax(T newMax) {
		max = newMax;
		if(max.compareTo(min) < 0) min = max;
	}
	
	public boolean contains(T val) {
		if(min == null || max == null || val == null) return true;
		return val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
	}
	
	@Override
	public String toString() {
		if(min.equals(max)) return min.toString();
		return min.toString() + "-" + max.toString();
	}
	
	public abstract T getRandomIn(Random rng);
	
	protected abstract T staticParse(String val);
	
	private static String[] splitRange(String range) {
		return range.split("[~-]");
	}
	
	protected static <T extends Number & Comparable<T>> Range<T> parse(String range, Range<T> template) {
		try {
			String[] split = splitRange(range);
			T hi, lo = template.staticParse(split[0]);
			if(split.length == 1)
				hi = lo;
			else hi = template.staticParse(split[1]);
			if(lo.compareTo(hi) < 0) {
				template.min = lo;
				template.max = hi;
			} else {
				template.min = hi;
				template.max = lo;
			}
			return template;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
//	public static Range<Integer> parseIntRange(String range) {
//		try {
//			String[] split = splitRange(range);
//			Integer hi, lo = Integer.valueOf(split[0]);
//			if(split.length == 1)
//				hi = lo;
//			else hi = Integer.valueOf(split[1]);
//			return new Range<Integer>(lo, hi);
//		} catch(NumberFormatException e) {
//			throw new IllegalArgumentException(e);
//		}
//	}
//	
//	public static Range<Double> parseDoubleRange(String range) {
//		try {
//			String[] split = splitRange(range);
//			Double hi, lo = Double.valueOf(split[0]);
//			if(split.length == 1)
//				hi = lo;
//			else hi = Double.valueOf(split[1]);
//			return new Range<Double>(lo, hi);
//		} catch(NumberFormatException e) {
//			throw new IllegalArgumentException(e);
//		}
//	}
}
