package com.gmail.zariust.otherdrops.options;

import java.util.Random;

public class IntRange extends Range<Integer> {
	public IntRange(){
		super(0);
	}
	
	public IntRange(Integer val) {
		super(val);
	}
	
	public IntRange(Integer lo, Integer hi) {
		super(lo, hi);
	}
	
	@Override
	public Integer getRandomIn(Random rng) {
		if(min.equals(max)) return min;
		return min + rng.nextInt(max - min + 1);
	}

	@Override
	public Integer negate(Integer num) {
		return -num;
	}

	@Override
	protected Integer staticParse(String val) {
		return Integer.parseInt(val);
	}
	
	public static IntRange parse(String val) {
		return (IntRange) Range.parse(val, new IntRange());
	}
	
	public DoubleRange toDoubleRange() {
		return new DoubleRange(getMin().doubleValue(), getMax().doubleValue());
	}
}
