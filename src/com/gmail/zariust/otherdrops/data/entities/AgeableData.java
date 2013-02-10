package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

/**
 * Used for Ageable mobs that don't have other data of interest
 * (as at 2013/02/09 - Chicken, Cow, MushroomCow, Pig
 * @author zarius
 *
 */
public class AgeableData extends CreatureData {
	Boolean adult = null; // null = wildcard
	Integer maxHealth = null;
	
	public AgeableData(Boolean adult, Integer health) {
		this.adult = adult;
		this.maxHealth = health;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Ageable) {
			Ageable z = (Ageable)mob;
			if (adult != null) if (adult == false) z.setBaby();
			if (maxHealth != null) {
				z.setMaxHealth(maxHealth);
				z.setHealth(maxHealth);
			}
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof AgeableData)) return false;
		
		AgeableData vd = (AgeableData)d;
		
		if (this.adult != null)
			if (this.adult != vd.adult) return false; 
		if (this.maxHealth != null)
			if (this.maxHealth != vd.maxHealth) return false; 
		
		return true;
	}

	public static CreatureData parseFromEntity(Entity entity) {
		if (entity instanceof Ageable) {
			return new AgeableData(((Ageable)entity).isAdult(), ((Ageable)entity).getMaxHealth());
		} else {
			Log.logInfo("AgeableData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}

	public static CreatureData parseFromString(String state) {
		// state example: VILLAGER!BABY, BABY, BABY!NORMAL (order doesn't matter)
		Boolean adult = null;
		Integer maxHealth = null;

		if (!state.isEmpty() && !state.equals("0")) {
			String split[] = state.split("!");

			for (String sub : split) {
				if (sub.matches("[0-9]+")) { // need to check numbers before any .toLowerCase()
					maxHealth = Integer.valueOf(sub);
				} else {
					sub = sub.toLowerCase().replaceAll("[ -_]",  "");
					if (sub.equalsIgnoreCase("adult"))      adult = true;
					else if (sub.equalsIgnoreCase("baby"))  adult = false;
				}
			}
		}

		return new AgeableData(adult, maxHealth);
	}
	
	public String toString() {
		String val = "";
		if (adult != null) {
			val += "!";
			val += adult?"ADULT":"BABY";
		}
		return val;
	}
	
	@Override
	public String get(Enum<?> creature) {
		if(creature instanceof EntityType) return this.toString();
		return "";
	}
	
}
