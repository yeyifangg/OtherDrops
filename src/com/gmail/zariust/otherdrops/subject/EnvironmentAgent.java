// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.subject;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import static com.gmail.zariust.common.CommonPlugin.enumValue;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.ToolDamage;

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
		return new HashCode(this).get(dmg);
	}
	
	public DamageCause getDamage() {
		return dmg;
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.DAMAGE;
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(ToolDamage amount, Random rng) {}

	public static EnvironmentAgent parse(String name, String data) {
		name = name.toUpperCase().replace("DAMAGE_", "");
		DamageCause cause;
		try {
			cause = enumValue(DamageCause.class, name);
			if(cause == DamageCause.FIRE_TICK || cause == DamageCause.CUSTOM) return null;
			else if(cause == DamageCause.FIRE) cause = DamageCause.FIRE_TICK;
		} catch(IllegalArgumentException e) {
			if(name.equals("WATER")) cause = DamageCause.CUSTOM;
			else if(name.equals("BURN")) cause = DamageCause.FIRE_TICK;
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
			CreatureType creature = CommonEntity.getCreatureType(data);
			if(creature != null) return creature;
			if(data.equalsIgnoreCase("PLAYER")) return ItemCategory.PLAYER;
			if(data.equalsIgnoreCase("FIREBALL")) return ItemCategory.EXPLOSION;
			break;
		case FALL:
			// TODO: Specify distance?
			if (data.isEmpty()) data = "0";
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
		else if(dmg == DamageCause.FIRE_TICK) return "DAMAGE_BURN";
		return "DAMAGE_" + dmg.toString();
	}

	@Override
	public Data getData() {
		return null;
	}
}
