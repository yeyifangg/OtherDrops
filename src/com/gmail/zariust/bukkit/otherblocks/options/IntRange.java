package com.gmail.zariust.bukkit.otherblocks.options;

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
	protected Integer staticParse(String val) {
		return Integer.valueOf(val);
	}
	
	public static IntRange parse(String val) {
		return (IntRange) Range.parse(val, new IntRange());
	}
}
