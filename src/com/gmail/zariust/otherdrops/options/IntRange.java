package com.gmail.zariust.otherdrops.options;

import java.util.Random;

import com.gmail.zariust.otherdrops.OtherDrops;

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
	public Integer getRandomIn() {
		if(min.equals(max)) return min;
		return min + OtherDrops.rng.nextInt(max - min + 1);
	}

	@Override
	protected Integer staticParse(String val) {
		return Integer.valueOf(val);
	}
	
	public static IntRange parse(String val) {
		return (IntRange) Range.parse(val, new IntRange());
	}
}
