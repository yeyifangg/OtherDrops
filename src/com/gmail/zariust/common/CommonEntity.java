package com.gmail.zariust.common;

import org.bukkit.Material;
import org.bukkit.entity.*;

public final class CommonEntity {
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
}
