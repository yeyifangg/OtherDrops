package com.gmail.zariust.bukkit.otherblocks.subject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

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

	public static EnvironmentAgent parse(String name, String data) {
		DamageCause cause;
		try {
			cause = DamageCause.valueOf(name);
			if(cause == DamageCause.FIRE_TICK || cause == DamageCause.CUSTOM) return null;
		} catch(IllegalArgumentException e) {
			if(name.equals("DAMAGE_WATER")) cause = DamageCause.CUSTOM;
			else return null;
		}
		// TODO: Make use of this
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
			if(data.equalsIgnoreCase("PLAYER")) return ItemType.PLAYER;
			if(data.equalsIgnoreCase("FIREBALL")) return ItemType.EXPLOSION;
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
}
