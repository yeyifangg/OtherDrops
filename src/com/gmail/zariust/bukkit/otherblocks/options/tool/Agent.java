package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;

public class Agent {
	public enum ToolType {ITEM, CREATURE, PROJECTILE, DAMAGE, SPECIAL};
	public final static Agent ANY = new Agent((ToolType) null);
	public final static Agent ANY_ITEM = new PlayerAgent((Material) null);
	public final static Agent ANY_CREATURE = new CreatureAgent(null);
	public final static Agent ANY_DAMAGE = new EnvironmentAgent(null);
	public final static Agent ANY_PROJECTILE = new ProjectileAgent(null, null);
	public final static Agent LEAF_DECAY = new Agent(ToolType.SPECIAL);
	public final static Agent FLOW = new Agent(ToolType.SPECIAL);

	private ToolType type;
	
	protected Agent(ToolType t) {
		type = t;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Agent)) return false;
		return equals((Agent) other);
	}
	
	public boolean equals(Agent other) {
		if(type == null || other.type == null) return true;
		if(type != other.type) return false;
		return matches(other);
	}

	protected boolean matches(Agent other) {
		return this == other;
	}

	@Override
	public int hashCode() {
		short t = (short) type.hashCode();
		int v = getIdHash(), data = getDataHash();
		return (v << 16) | t | (data << 3);
	}

	protected int getDataHash() {
		return 7;
	}

	protected int getIdHash() {
		return -42;
	}

	public ToolType getType() {
		return type;
	}
	
	public void damage(int amount) {}

	public void damageTool(short amount) {}
	
	public void damageTool() {}
}
