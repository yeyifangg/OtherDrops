package com.gmail.zariust.bukkit.otherblocks.options;

public class Target {
	public enum TargetType {BLOCK, CREATURE, PLAYER, GROUP, SPECIAL};
	public final static Target LEAF_DECAY = new Target(TargetType.SPECIAL);
	
	private TargetType type;
	private Material mat;
	private int data;
	
	public Target(TargetType t)
}
