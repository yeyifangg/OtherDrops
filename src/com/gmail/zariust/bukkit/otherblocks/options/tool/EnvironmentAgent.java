package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class EnvironmentAgent extends Agent {
	private DamageCause dmg;
	
	public EnvironmentAgent(DamageCause tool) {
		super(ToolType.DAMAGE);
		dmg = tool;
	}
	
	@Override
	protected boolean matches(Agent other) {
		if(other instanceof EnvironmentAgent) return matches((EnvironmentAgent) other);
		return false;
	}
	
	private boolean matches(EnvironmentAgent other) {
		if(dmg == null || other.dmg == null) return true;
		return dmg == other.dmg;
	}
	
	@Override
	protected int getIdHash() {
		return dmg == null ? 0 : dmg.hashCode();
	}
	
	@Override
	protected int getDataHash() {
		return 11;
	}
	
	public DamageCause getDamage() {
		return dmg;
	}
}
