package com.gmail.zariust.otherdrops.options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Biome;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;

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
		List<String> weather = OtherDropsConfig.getMaybeList(node, "weather");
		if(weather.isEmpty()) return def;
		Map<Weather, Boolean> result = new HashMap<Weather,Boolean>();
		result.put(null, OtherDropsConfig.containsAll(weather));
		for(String name : weather) {
			Weather storm = parse(name);
			if(storm != null) result.put(storm, true);
			else if(name.startsWith("-")) {
				result.put(null, true);
				storm = parse(name.substring(1));
				if(storm == null) {
					OtherDrops.logWarning("Invalid weather " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			}
		}
		if(result.isEmpty()) return null;
		return result;
	}
}
