package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;

public enum Weather {
	RAIN(true), SNOW(true), THUNDER(true), CLEAR(false), CLOUD(true), NONE(false),
	STORM(true) {
		@Override public boolean matches(Weather sky) {
			if(sky.stormy && sky != THUNDER) return true;
			return false;
		}
	};
	private boolean stormy;
	private static Map<String, Weather> nameLookup = new HashMap<String, Weather>();
	
	static {
		for(Weather storm : values())
			nameLookup.put(storm.name(), storm);
	}
	
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
	
	public static Weather parse(String storm) {
		return nameLookup.get(storm.toUpperCase());
	}

	public static Map<Weather, Boolean> parseFrom(ConfigurationNode node, Map<Weather, Boolean> def) {
		List<String> weather = OtherBlocksConfig.getMaybeList(node, "weather");
		if(weather.isEmpty()) return def;
		Map<Weather, Boolean> result = new HashMap<Weather,Boolean>();
		for(String name : weather) {
			Weather storm = parse(name);
			if(storm == null && name.startsWith("-")) {
				storm = parse(name.substring(1));
				if(storm == null) {
					OtherBlocks.logWarning("Invalid weather " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			} else result.put(storm, true);
		}
		if(result.isEmpty()) return null;
		return result;
	}
}
