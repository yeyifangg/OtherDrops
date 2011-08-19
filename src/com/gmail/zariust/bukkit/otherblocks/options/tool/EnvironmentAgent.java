package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public class EnvironmentAgent implements Agent {
	private DamageCause dmg;
	
	public EnvironmentAgent() {
		this(null);
	}
	
	public EnvironmentAgent(DamageCause tool) {
		dmg = tool;
	}
	
	private EnvironmentAgent equalsHelper(Object other) {
		if(!(other instanceof EnvironmentAgent)) return null;
		return (EnvironmentAgent) other;
	}

	private boolean isEqual(EnvironmentAgent tool) {
		if(tool == null) return false;
		return dmg == tool.dmg;
	}

	@Override
	public boolean equals(Object other) {
		EnvironmentAgent tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		// TODO: Is this right? Will all creature/player agents coincide with ENTITY_ATTACK and all projectile
		// agents with PROJECTILE?
		if(dmg == DamageCause.ENTITY_ATTACK && (other instanceof CreatureAgent || other instanceof PlayerAgent))
			return true;
		else if(dmg == DamageCause.PROJECTILE && other instanceof ProjectileAgent)
			return true;
		EnvironmentAgent tool = equalsHelper(other);
		if(dmg == null) return true;
		return isEqual(tool);
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.DAMAGE, dmg == null ? 0 : dmg.hashCode(), 11);
	}
	
	public DamageCause getDamage() {
		return dmg;
	}

	@Override
	public ItemType getType() {
		return ItemType.DAMAGE;
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}
}
