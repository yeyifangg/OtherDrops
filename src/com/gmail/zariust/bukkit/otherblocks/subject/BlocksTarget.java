package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

@ConfigOnly(BlockTarget.class)
public class BlocksTarget implements Target {
	private MaterialGroup group;
	
	public BlocksTarget(MaterialGroup grp) {
		group = grp;
	}
	
	public MaterialGroup getGroup() {
		return group;
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof BlocksTarget)) return false;
		return group == ((BlocksTarget) other).group;
	}

	@Override
	public int hashCode() {
		return group.hashCode();
	}
	
	@Override
	public boolean matches(Subject other) {
		if(!(other instanceof BlockTarget)) return false;
		BlockTarget block = (BlockTarget) other;
		return group.contains(block.getMaterial());
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.BLOCK;
	}
	
	@Override
	public String toString() {
		if(group == null) return "ANY_BLOCK";
		return group.toString();
	}

	@Override
	public List<Target> canMatch() {
		List<Target> all = new ArrayList<Target>();
		List<Material> materials = group.materials();
		for(Material block : materials) all.add(new BlockTarget(block));
		return all;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override // Should never be called due to the annotation
	public Location getLocation() {
		return null;
	}
}
