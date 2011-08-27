package com.gmail.zariust.otherdrops.options;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.EffectData;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.config.ConfigurationNode;

public class SoundEffect {
	private Effect type;
	// TODO: Would be nice to include note block sounds in here (missing API though)
	private EffectData data;
	
	public SoundEffect(Effect effect) {
		this(effect, null);
	}
	
	public SoundEffect(Effect effect, EffectData d) {
		type = effect;
		data = d;
	}

	public void play(Location location) {
		if(type != null)
			location.getWorld().playEffect(location, type, data.getData(), data.getRadius());
	}

	public static SoundEffect parse(String key) {
		String[] split = key.split("@");
		String name = split[0], data = "";
		if(split.length > 1) data = split[1];
		else split = null;
		try {
			Effect effect = Effect.valueOf(name);
			if(effect == null) return null;
			EffectData state = EffectData.parse(effect, data);
			return new SoundEffect(effect, state);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	public static Set<SoundEffect> parseFrom(ConfigurationNode node) {
		List<String> effects = OtherDropsConfig.getMaybeList(node, "effect");
		if(effects.isEmpty()) return null;
		Set<SoundEffect> result = new HashSet<SoundEffect>();
		for(String name : effects) {
			SoundEffect effect = parse(name);
			if(effect == null) {
				OtherDrops.logWarning("Invalid effect " + name + "; skipping...");
				continue;
			}
			result.add(effect);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	@Override
	public String toString() {
		String ret = type.toString();
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(type);
		return ret;
	}
}
