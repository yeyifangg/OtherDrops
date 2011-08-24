package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;
import com.gmail.zariust.bukkit.otherblocks.data.Data;
import com.gmail.zariust.bukkit.otherblocks.data.EffectData;
import com.gmail.zariust.bukkit.otherblocks.data.RecordData;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.config.ConfigurationNode;

public class SoundEffect {
	private Effect type;
	// TODO: Would be nice to include note block sounds in here (missing API though)
	private Data data;
	
	public SoundEffect(Effect effect) {
		this(effect, null);
	}
	
	public SoundEffect(Effect effect, Data d) {
		type = effect;
		data = d;
	}

	public void play(Location location) {
		if(type != null)
			location.getWorld().playEffect(location, type, data.getData());
	}

	public static SoundEffect parse(String key) {
		String[] split = key.split("@");
		String name = split[0];
		if(split.length > 1) split = split[1].split("/");
		else split = null;
		try {
			Effect effect = Effect.valueOf(name);
			if(effect == null) return null;
			Data data;
			switch(effect) {
			case RECORD_PLAY:
				data = RecordData.parse(key);
				break;
			case SMOKE:
				BlockFace face = BlockFace.valueOf(key);
				if(face == null) return null;
				data = new EffectData(face);
				break;
			case STEP_SOUND: // apparently this is actually BLOCK_BREAK
				Material mat = Material.getMaterial(key);
				if(mat == null) return null;
				data = new EffectData(mat);
				break;
			default:
				return new SoundEffect(effect);
			}
			return new SoundEffect(effect, data);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	public static Set<SoundEffect> parseFrom(ConfigurationNode node) {
		List<String> effects = OtherBlocksConfig.getMaybeList(node, "effect");
		if(effects.isEmpty()) return null;
		Set<SoundEffect> result = new HashSet<SoundEffect>();
		for(String name : effects) {
			SoundEffect effect = parse(name);
			if(effect == null) {
				OtherBlocks.logWarning("Invalid effect " + name + "; skipping...");
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
