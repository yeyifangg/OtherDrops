package com.gmail.zariust.bukkit.otherblocks.options;

public enum Time {
	DAY(0,12000-1), NIGHT(13800,22200-1), DUSK(12000,13800-1), DAWN(22200,24000-1);
	private Range<Long> range;
	
	private Time(long lo, long hi) {
		range = new Range<Long>(lo, hi);
	}

	public boolean contains(long time) {
		return range.contains(time);
	}
}
