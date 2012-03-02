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

package com.gmail.zariust.common;

import org.bukkit.Material;
//import static org.bukkit.Material.*;
import org.bukkit.entity.*;

import static org.bukkit.entity.EntityType.*;
import org.bukkit.material.MaterialData;

import com.gmail.zariust.otherdrops.OtherDrops;

public final class CommonEntity {

	/** Return a EntityType if the given string is a valid type or alias for a creature.  Ignore non creature
	 *  entities unless prefixed with CREATURE_ or ENTITY_
	 *  We could use EntityType.fromName(string) but it would not be case (or dash) insensitive.
	 *  
	 * @param name - spaces, dashes and underscores are ignored, case insensitive
	 * @return EntityType or null if no valid type
	 */
	public static EntityType getCreatureEntityType(String name) {
		if (name == null || name.isEmpty()) return null;
		String originalName = name;
		name = name.split("@")[0].toLowerCase(); // remove data value, if any, and make **lowercase** (keep in mind below)
		name = name.replace("creature_", "");
		name = name.replace("entity_", "");
		//OtherDrops.logInfo("Checking creature '"+name+"' (original name: '"+originalName+"')", Verbosity.HIGH);
		name = name.replace("[ -_]", "");        // remove spaces, dashes & underscores

		// Creature aliases - format: (<aliasvalue>, <bukkitmobname>) - must be lowercase
		name = name.replace("mooshroom", "mushroomcow");
		name = name.replace("endermen", "enderman");
		
		for (EntityType creature : EntityType.values())
		{
			if (name.equalsIgnoreCase(creature.name().toLowerCase().replaceAll("[ -_]", ""))) 
				if (creature.isAlive())	{
					return creature;
				}
		}
		return null;
	}
	
	public static Material getVehicleType(Entity e) {
		if(e instanceof Boat)			 return Material.BOAT;
		if(e instanceof PoweredMinecart) return Material.POWERED_MINECART;
		if(e instanceof StorageMinecart) return Material.STORAGE_MINECART;
		if(e instanceof Minecart)		 return Material.MINECART;
		return null;
	}
	
	public static Material getProjectileType(Entity e) {
		if(e instanceof Arrow)		return Material.ARROW;
		if(e instanceof Fish)		return Material.FISHING_ROD;
		if(e instanceof Fireball)	return Material.FIRE;
		if(e instanceof Egg)		return Material.EGG;
		if(e instanceof Snowball)	return Material.SNOW_BALL;
		return null;
	}

	public static int getCreatureData(Entity entity) {
		if(entity == null) return 0;
		EntityType creatureType = entity.getType();
		if(creatureType == null) return 0;
		switch(creatureType) {
		case CREEPER:
			return ((Creeper)entity).isPowered() ? 1 : 0;
		case PIG:
			return ((Pig)entity).hasSaddle() ? 1 : 0;
		case SHEEP:
			return ((Sheep)entity).getColor().getData() + (((Sheep)entity).isSheared() ? 32 : 0);
		case SLIME:
			return ((Slime)entity).getSize();
		case WOLF:
			return ((Wolf)entity).isAngry() ? 1 : (((Wolf)entity).isTamed() ? 2 : 0);
		case PIG_ZOMBIE:
			return ((PigZombie)entity).getAnger();
		case ENDERMAN:
			MaterialData data = ((Enderman)entity).getCarriedMaterial();
			if(data == null) return 0;
			return data.getItemTypeId() | (data.getData() << 8);
		case OCELOT:
			switch (((Ocelot)entity).getCatType()) {
			case WILD_OCELOT:
				return 0;
			case BLACK_CAT:
				return 1;
			case RED_CAT:
				return 2;
			case SIAMESE_CAT:
				return 3;
			}
		default:
			return 0;
		}
	}

	public static Material getExplosiveType(Entity e) {
		if(e instanceof Fireball)	return Material.FIRE;
		if(e instanceof TNTPrimed)	return Material.TNT;
		return null;
	}
}
