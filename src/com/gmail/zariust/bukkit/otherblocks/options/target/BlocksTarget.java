package com.gmail.zariust.bukkit.otherblocks.options.target;

import com.gmail.zariust.bukkit.common.MaterialGroup;

public class BlocksTarget extends Target {
	MaterialGroup group;
	
	public BlocksTarget(MaterialGroup grp) {
		super(TargetType.BLOCK);
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
}
