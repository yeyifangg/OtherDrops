package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.Random;
import static java.lang.Math.abs;

public class Time extends Range<Long> {
	public final static Time DAY = new Time(0,12000-1);
	public final static Time NIGHT = new Time(13800,22200-1);
	public final static Time DUSK = new Time(12000,13800-1);
	public final static Time DAWN = new Time(22200,24000-1);
	
	public Time() {
		super((long) 0);
	}
	
	public Time(Long val) {
		super(val);
	}
	
	public Time(Long lo, Long hi) {
		super(lo, hi);
	}
	
	public Time(int val) {
		this((long) val);
	}
	
	public Time(int lo, int hi) {
		this((long) lo,(long) hi);
	}

	@Override
	public Long getRandomIn(Random rng) {
		if(min.equals(max)) return min;
		return min + abs(rng.nextLong() % (max - min + 1));
	}

	@Override
	protected Long staticParse(String val) {
		return Long.valueOf(val);
	}
	
	public static Time parse(String val) {
		return (Time) Range.parse(val, new Time());
	}
}
