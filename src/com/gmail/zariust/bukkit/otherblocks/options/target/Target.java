package com.gmail.zariust.bukkit.otherblocks.options.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.MaterialGroup;

public abstract class Target {
	public enum TargetType {BLOCK, CREATURE, PLAYER, SPECIAL};
	
	private TargetType type;
	
	protected Target(TargetType t) {
		type = t;
	}
	
	public TargetType getType() {
		return type;
	}

	public static Target parseName(String blockName) {
		String[] split = blockName.split("@");
		String name = split[0].toUpperCase(), data = "";
		Integer intData;
		if(split.length > 1) data = split[1];
		// Name is one of the following:
		// - A Material constant that is a block, painting, or vehicle
		// - A CreatureType constant prefixed by CREATURE_
		// - An integer representing a Material
		// - One of the keywords PLAYER or PLAYERGROUP
		// - A MaterialGroup constant containing blocks
		if(name.equals("PLAYER")) return new PlayerTarget(data);
		else if(name.equals("PLAYERGROUP")) return new GroupTarget(data);
		else {
			if(data.isEmpty()) intData = null;
			else try {
				intData = Integer.parseInt(data);
			} catch(NumberFormatException e) {
				intData = (int) CommonMaterial.getAnyDataShort(name, data);
			}
			if(name.startsWith("ANY_")) {
				MaterialGroup group = MaterialGroup.get(name);
				if(group != null) return new BlocksTarget(group);
				else return null;
			} else if(name.startsWith("CREATURE_")) {
				// TODO: Is there a way to detect non-vanilla creatures?
				CreatureType mob = CreatureType.fromName(name.replace("CREATURE_", ""));
				if(mob != null) return new CreatureTarget(mob, intData);
				else return null;
			} else try {
				int id = Integer.parseInt(name);
				// TODO: Need some way to determine whether the ID is valid WITHOUT using only Material
				// Does ItemCraft have API for this?
				return new BlockTarget(id, intData);
			} catch(NumberFormatException x) {
				Material mat = Material.getMaterial(name);
				if(!mat.isBlock()) {
					// Only a very select few non-blocks are permitted as a target
					if(mat != Material.PAINTING && mat != Material.BOAT && mat != Material.MINECART &&
							mat != Material.POWERED_MINECART && mat != Material.STORAGE_MINECART)
						return null;
				}
				if(mat != null) return new BlockTarget(mat, intData);
				else return null;
			}
		}
	}
	
	public abstract boolean overrideOn100Percent();
	
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract int hashCode();

	public boolean matches(Target block) {
		return equals(block);
	}
}
