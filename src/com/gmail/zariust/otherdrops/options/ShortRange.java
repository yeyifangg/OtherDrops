package com.gmail.zariust.otherdrops.options;

import java.util.Random;

import com.gmail.zariust.otherdrops.OtherDrops;

public class ShortRange extends Range<Short> {
	public ShortRange() {
		super((short) 0);
	}
	
	public ShortRange(Short val) {
		super(val);
	}
	
	public ShortRange(Short lo, Short hi) {
		super(lo, hi);
	}

	@Override
	public Short getRandomIn() {
		if(min.equals(max)) return min;
		return (short) (min + OtherDrops.rng.nextInt(max - min + 1));
	}

	@Override
	protected Short staticParse(String val) {
		return Short.valueOf(val);
	}
	
	public static ShortRange parse(String val) {
		return (ShortRange) Range.parse(val, new ShortRange());
	}
}
