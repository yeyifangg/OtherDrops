package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.block.Block;

public class Target {
	public enum TargetType {BLOCK, CREATURE, PLAYER, SPECIAL};
	public final static Target LEAF_DECAY = new Target(TargetType.SPECIAL);
	
	private TargetType type;
	
	protected Target(TargetType t) {
		type = t;
	}
}
