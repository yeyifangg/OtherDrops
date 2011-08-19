package com.gmail.zariust.bukkit.common;

import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.*;

public class CommonEntity {
	
	public static Set<String> getValidSynonyms() {
		return CreatureGroup.all();
	}
	
	public static boolean isValidSynonym(String string) {
		return CreatureGroup.isValid(string);
	}
	
	public static boolean isSynonymFor(String string, CreatureType material) {
		if(!isValidSynonym(string)) return false;
		return CreatureGroup.get(string).contains(material);
	}
	
	public static CreatureType getCreatureType(Entity e) {
		
		if(e instanceof Chicken)	return CreatureType.CHICKEN;
		if(e instanceof Cow)		return CreatureType.COW;
		if(e instanceof Creeper)	return CreatureType.CREEPER;
		if(e instanceof Ghast)		return CreatureType.GHAST;
		if(e instanceof Giant)		return CreatureType.GIANT;
		if(e instanceof Pig)		return CreatureType.PIG;
		if(e instanceof PigZombie)	return CreatureType.PIG_ZOMBIE;
		if(e instanceof Sheep)		return CreatureType.SHEEP;
		if(e instanceof Skeleton)	return CreatureType.SKELETON;
		if(e instanceof Slime)	 	return CreatureType.SLIME;
		if(e instanceof Spider)	 	return CreatureType.SPIDER;
		if(e instanceof Squid)	 	return CreatureType.SQUID;
		if(e instanceof Wolf)	 	return CreatureType.WOLF;
		// Remember that Zombie must come after PigZombie
		if(e instanceof Zombie)	 	return CreatureType.ZOMBIE;

		// Monster last - is a supertype!
		if(e instanceof Monster) return CreatureType.MONSTER;
		
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
		switch(getCreatureType(entity)) {
		case CREEPER:
			return ((Creeper)entity).isPowered() ? 1 : 0;
		case PIG:
			return ((Pig)entity).hasSaddle() ? 1 : 0;
		case SHEEP:
			return ((Sheep)entity).getColor().getData() + (((Sheep)entity).isSheared() ? 16 : 0);
		case SLIME:
			return ((Slime)entity).getSize();
		case WOLF:
			return ((Wolf)entity).isAngry() ? 1 : (((Wolf)entity).isTamed() ? 2 : 0);
		case PIG_ZOMBIE:
			return ((PigZombie)entity).getAnger();
		default:
			return 0;
		}
	}

	public static Material getExplosiveType(Entity e) {
		
		if(e instanceof Fireball)	return Material.FIRE;
		if(e instanceof TNTPrimed)	return Material.TNT;
		
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public static Integer parseCreatureData(CreatureType creature, String state) {
		switch(creature) {
		case CREEPER:
			if(state.equalsIgnoreCase("POWERED")) return 1;
			else if(state.equalsIgnoreCase("UNPOWERED")) return 0;
			break;
		case PIG:
			if(state.equalsIgnoreCase("SADDLED")) return 1;
			else if(state.equalsIgnoreCase("UNSADDLED")) return 0;
			break;
		case SHEEP:
			// For sheep we have 1 as white so on so that 0 can (hopefully) mean the default of a random natural colour
			String[] split = state.split("/");
			if(split.length <= 2) {
				String colour = "", wool = "";
				if(split[0].endsWith("SHEARED")) {
					wool = split[0];
					if(split.length == 2) colour = split[1];
				} else if(split.length == 2 && split[1].endsWith("SHEARED")) {
					wool = split[1];
					colour = split[0];
				} else colour = split[0];
				if(!colour.isEmpty() || !wool.isEmpty()) {
					boolean success;
					int data = 0;
					if(!colour.isEmpty()) {
						try {
							data = DyeColor.valueOf(colour).getData() + 1;
							success = true;
						} catch(IllegalArgumentException e) {
							success = false;
						}
						// Or numbers
						try {
							int clr = Integer.parseInt(colour);
							if(clr < 16) data = clr + 1;
						} catch(NumberFormatException e) {}
					} else success = true;
					if(wool.equalsIgnoreCase("SHEARED")) return data + 32;
					else if(success || wool.equalsIgnoreCase("UNSHEARED")) return data;
				}
			}
			break;
		case SLIME:
			if(state.equalsIgnoreCase("TINY")) return 1;
			else if(state.equalsIgnoreCase("SMALL")) return 2;
			else if(state.equalsIgnoreCase("BIG")) return 3;
			else if(state.equalsIgnoreCase("HUGE")) return 4;
			// Fallthrough intentional
		case PIG_ZOMBIE:
			try {
				int sz = Integer.parseInt(state);
				return sz;
			} catch(NumberFormatException e) {}
			break;
		case WOLF:
			if(state.equalsIgnoreCase("TAME") || state.equalsIgnoreCase("TAMED"))
				return 2;
			else if(state.equalsIgnoreCase("WILD") || state.equalsIgnoreCase("NEUTRAL"))
				return 0;
			else if(state.equalsIgnoreCase("ANGRY")) return 1;
			break;
		}
		return null;
	}
}
