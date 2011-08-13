package com.gmail.zariust.bukkit.otherblocks.options;

import org.bukkit.block.Biome;

public enum Weather {
	RAIN, SNOW, THUNDER, CLEAR, CLOUD, STORM, NONE;

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
}
