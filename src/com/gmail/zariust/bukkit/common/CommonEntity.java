package com.gmail.zariust.bukkit.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.*;

public class CommonEntity {
	
	// Synonyms between entities

	private static final Map<String, List<CreatureType>> SYNONYMS = initSynonyms();

	private static Map<String, List<CreatureType>> initSynonyms() {
		Map<String, List<CreatureType>> result = new HashMap<String, List<CreatureType>>();
		// Alignments
		result.put("CREATURE_HOSTILE",	Arrays.asList(CreatureType.CREEPER, CreatureType.GHAST, CreatureType.GIANT, CreatureType.MONSTER, CreatureType.SKELETON, CreatureType.SLIME, CreatureType.SPIDER, CreatureType.ZOMBIE));
		result.put("CREATURE_FRIENDLY", Arrays.asList(CreatureType.COW, CreatureType.CHICKEN, CreatureType.PIG, CreatureType.SHEEP, CreatureType.SQUID));
		result.put("CREATURE_NEUTRAL",	Arrays.asList(CreatureType.PIG_ZOMBIE, CreatureType.WOLF));
		// Categories
		result.put("CREATURE_ANIMAL", Arrays.asList(CreatureType.COW, CreatureType.CHICKEN, CreatureType.PIG, CreatureType.SHEEP, CreatureType.WOLF));
		result.put("CREATURE_UNDEAD", Arrays.asList(CreatureType.PIG_ZOMBIE, CreatureType.ZOMBIE, CreatureType.SKELETON));
		// Any
		List<CreatureType> merger;
		
		merger = new ArrayList<CreatureType>();
		merger.addAll(result.get("CREATURE_HOSTILE"));
		merger.addAll(result.get("CREATURE_FRIENDLY"));
		merger.addAll(result.get("CREATURE_NEUTRAL"));
		result.put("CREATURE_ANY", merger);
		
		return Collections.unmodifiableMap(result);
	}
	
	public static Set<String> getValidSynonyms() {
		return SYNONYMS.keySet();
	}
	
	public static boolean isValidSynonym(String string) {
		return SYNONYMS.containsKey(string);
	}
	
	public static boolean isSynonymFor(String string, CreatureType material) {
		if(!isValidSynonym(string)) return false;
		return SYNONYMS.get(string).contains(material);
	}
	
	public static CreatureType getCreatureType(Entity e) {
		
		if(e instanceof Chicken)   return CreatureType.CHICKEN;
		if(e instanceof Cow)	   return CreatureType.COW;
		if(e instanceof Creeper)   return CreatureType.CREEPER;
		if(e instanceof Ghast)	   return CreatureType.GHAST;
		if(e instanceof Giant)	   return CreatureType.GIANT;
		if(e instanceof Pig)	   return CreatureType.PIG;
		if(e instanceof PigZombie) return CreatureType.PIG_ZOMBIE;
		if(e instanceof Sheep)	   return CreatureType.SHEEP;
		if(e instanceof Skeleton)  return CreatureType.SKELETON;
		if(e instanceof Slime)	   return CreatureType.SLIME;
		if(e instanceof Spider)	   return CreatureType.SPIDER;
		if(e instanceof Squid)	   return CreatureType.SQUID;
		if(e instanceof Wolf)	   return CreatureType.WOLF;
		if(e instanceof Zombie)	   return CreatureType.ZOMBIE;

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
}
