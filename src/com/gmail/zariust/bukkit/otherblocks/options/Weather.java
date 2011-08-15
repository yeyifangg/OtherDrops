package com.gmail.zariust.bukkit.otherblocks.options;

import org.bukkit.block.Biome;

public enum Weather {
	RAIN(true), SNOW(true), THUNDER(true), CLEAR(false), CLOUD(true), NONE(false),
	STORM(true) {
		@Override public boolean matches(Weather sky) {
			if(sky.stormy && sky != THUNDER) return true;
			return false;
		}
	};
	private boolean stormy;
	
	private Weather(boolean storm) {
		stormy = storm;
	}

	public static Weather match(Biome biome, boolean hasStorm, boolean thundering) {
		switch(biome) {
		case HELL:
			return NONE;
		case SKY:
		case DESERT:
		case ICE_DESERT: // TODO: Do ice deserts get snow or not?
			if(hasStorm) return CLOUD;
			return CLEAR;
		case TUNDRA:
		case TAIGA:
			if(hasStorm) return SNOW;
			return CLEAR;
		default:
			if(hasStorm) return thundering ? THUNDER : RAIN;
			return CLEAR;
		}
	}

	public boolean isStormy() {
		return stormy;
	}

	public boolean matches(Weather sky) {
		if(stormy && sky == STORM) return true;
		return this == sky;
	}
}
