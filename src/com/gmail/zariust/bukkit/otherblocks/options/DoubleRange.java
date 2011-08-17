package com.gmail.zariust.bukkit.otherblocks.options;

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
		double random = rng.nextDouble() * (max - min + 1);
		if(random > max) random = max;
		return min + random;
	}
	
	@Override
	protected Double staticParse(String val) {
		return Double.valueOf(val);
	}
	
	public static DoubleRange parse(String val) {
		return (DoubleRange) Range.parse(val, new DoubleRange());
	}
}
