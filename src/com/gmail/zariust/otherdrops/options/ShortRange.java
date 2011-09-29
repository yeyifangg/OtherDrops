package com.gmail.zariust.otherdrops.options;

import java.util.Random;

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
	public Short getRandomIn(Random rng) {
		if(min.equals(max)) return min;
		return (short) (min + rng.nextInt(max - min + 1));
	}

	@Override
	public Short negate(Short num) {
		return (short) -num;
	}

	@Override
	protected Short staticParse(String val) {
		return Short.parseShort(val);
	}
	
	public static ShortRange parse(String val) {
		return (ShortRange) Range.parse(val, new ShortRange());
	}
}
