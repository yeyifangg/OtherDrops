package com.gmail.zariust.bukkit.otherblocks.options.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.MaterialGroup;

public class Target {
	public enum TargetType {BLOCK, CREATURE, PLAYER, SPECIAL};
	public final static Target LEAF_DECAY = new Target(TargetType.SPECIAL);
	
	private TargetType type;
	
	protected Target(TargetType t) {
		type = t;
	}
	
	public TargetType getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Target)) return false;
		return type == ((Target)other).type;
	}

	@Override
	public int hashCode() {
		return -121;
	}

	public static List<Target> parseName(String blockName) {
		String[] split = blockName.split("@");
		String name = split[0].toUpperCase(), data = "";
		int intData = 0;
		if(split.length > 1) data = split[1];
		List<Target> targets;
		// Name is one of the following:
		// - A Material constant that is a block, painting, or vehicle
		// - A CreatureType constant prefixed by CREATURE_
		// - An integer representing a Material
		// - One of the keywords PLAYER, PLAYERGROUP, or SPECIAL_LEAFDECAY
		// - A MaterialGroup constant containing blocks
		if(name.equals("SPECIAL_LEAFDECAY")) targets = Collections.singletonList(LEAF_DECAY);
		else if(name.equals("PLAYER")) targets = Collections.singletonList((Target) new PlayerTarget(data));
		else if(name.equals("PLAYERGROUP")) targets = Collections.singletonList((Target) new GroupTarget(data));
		else try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {
			intData = CommonMaterial.getAnyDataShort(name, data);
		} finally {
			if(name.startsWith("ANY_")) {
				MaterialGroup group = MaterialGroup.get(name);
				targets = new ArrayList<Target>();
				for(Material mat : group.materials())
					targets.add(new BlockTarget(mat));
			} else if(name.startsWith("CREATURE_")) {
				String mob = name.replace("CREATURE_", "");
				targets = Collections.singletonList((Target) new CreatureTarget(CreatureType.fromName(mob), intData));
			} else try {
				int id = Integer.parseInt(name);
				targets = Collections.singletonList((Target) new BlockTarget(id, intData));
			} catch(NumberFormatException x) {
				targets = Collections.singletonList((Target) new BlockTarget(Material.getMaterial(name), intData));
			}
		}
		return targets;
	}
	
	public boolean overrideOn100Percent() {
		return true;
	}
}
