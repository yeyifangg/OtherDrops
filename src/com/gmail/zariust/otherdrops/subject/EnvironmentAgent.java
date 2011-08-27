package com.gmail.zariust.otherdrops.subject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.zariust.otherdrops.drops.AbstractDrop;

public class EnvironmentAgent implements Agent {
	private DamageCause dmg;
	// TODO: Need auxiliary data?
	
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
	public boolean matches(Subject other) {
		// TODO: Is this right? Will all creature/player agents coincide with ENTITY_ATTACK and all projectile
		// agents with PROJECTILE?
		if(dmg == DamageCause.ENTITY_ATTACK && (other instanceof CreatureSubject || other instanceof PlayerSubject))
			return true;
		else if(dmg == DamageCause.PROJECTILE && other instanceof ProjectileAgent)
			return true;
		EnvironmentAgent tool = equalsHelper(other);
		if(dmg == null) return true;
		return isEqual(tool);
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemCategory.DAMAGE, dmg == null ? 0 : dmg.hashCode(), 11);
	}
	
	public DamageCause getDamage() {
		return dmg;
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.DAMAGE;
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}

	public static EnvironmentAgent parse(String name, String data) {
		name = name.toUpperCase().replace("DAMAGE_", "");
		DamageCause cause;
		try {
			cause = DamageCause.valueOf(name);
			if(cause == DamageCause.FIRE_TICK || cause == DamageCause.CUSTOM) return null;
		} catch(IllegalArgumentException e) {
			if(name.equals("WATER")) cause = DamageCause.CUSTOM;
			else return null;
		}
		// TODO: Make use of this, somehow
		Object extra = parseData(cause, data);
		return new EnvironmentAgent(cause);
	}

	@SuppressWarnings("incomplete-switch")
	private static Object parseData(DamageCause cause, String data) {
		switch(cause) {
		case SUFFOCATION:
		case BLOCK_EXPLOSION:
		case CONTACT:
			// TODO: Specify block?
			return Material.getMaterial(data);
		case ENTITY_ATTACK:
		case ENTITY_EXPLOSION:
			// TODO: Specify entity?
			CreatureType creature = CreatureType.fromName(data);
			if(creature != null) return creature;
			if(data.equalsIgnoreCase("PLAYER")) return ItemCategory.PLAYER;
			if(data.equalsIgnoreCase("FIREBALL")) return ItemCategory.EXPLOSION;
			break;
		case FALL:
			// TODO: Specify distance?
			return Integer.parseInt(data);
		}
		return null;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String toString() {
		if(dmg == null) return "ANY_DAMAGE";
		else if(dmg == DamageCause.CUSTOM) return "DAMAGE_WATER";
		return "DAMAGE_" + dmg.toString();
	}
}
