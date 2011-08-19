package com.gmail.zariust.bukkit.otherblocks.options.target;

import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

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
	public boolean matches(Target other) {
		if(!(other instanceof BlockTarget)) return false;
		BlockTarget block = (BlockTarget) other;
		return group.contains(block.getMaterial());
	}

	@Override
	public ItemType getType() {
		return ItemType.BLOCK;
	}
	
	@Override
	public String toString() {
		return group.toString();
	}
}
