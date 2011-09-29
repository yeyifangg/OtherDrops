package com.gmail.zariust.otherdrops.options;

import java.util.Random;

public class DoubleRange extends Range<Double> {
	public DoubleRange() {
		super(0.0);
	}
	
	public DoubleRange(Double val) {
		super(val);
	}
	
	public DoubleRange(Double lo, Double hi) {
		super(lo, hi);
	}

	@Override
	public Double getRandomIn(Random rng) {
		if(min.equals(max)) return min;
		double random = ((rng.nextDouble()) * (max-min)) + min;
		// rng.nextDouble() returns a value between 0 & 1
		// example for min =  0, max =  1: (0*(1-0))+0 = 0, (1*(1-0))+0 = 1
		// example for min = 15, max = 20: (0*(20-15)+15=15, (1*(20-15))+15=20
		return random;
	}

	@Override
	public Double negate(Double num) {
		return -num;
	}
	
	@Override
	protected Double staticParse(String val) {
		return Double.parseDouble(val);
	}
	
	public static DoubleRange parse(String val) {
		return (DoubleRange) Range.parse(val, new DoubleRange());
	}
	
	public IntRange toIntRange() {
		return new IntRange(getMin().intValue(), getMax().intValue());
	}
}
