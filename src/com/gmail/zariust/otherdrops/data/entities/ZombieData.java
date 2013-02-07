package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

public class ZombieData extends CreatureData {
	Boolean villager = null; // null = wildcard
	Boolean adult = null;
	Integer maxHealth = null;
	
	public ZombieData(Boolean villager, Boolean adult, Integer health) {
		this.villager = villager;
		this.adult = adult;
		this.maxHealth = health;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Zombie) {
			Zombie z = (Zombie)mob;
			if (villager != null) if (villager) z.setVillager(true);
			if (adult != null) if (!adult) z.setBaby(true);
			if (maxHealth != null) {
				z.setMaxHealth(maxHealth);
				z.setHealth(maxHealth);
			}
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof ZombieData)) return false;
		
		ZombieData vd = (ZombieData)d;
		
		if (this.villager != null)
			if (this.villager != vd.villager) return false;
		if (this.adult != null)
			if (this.adult != vd.adult) return false; 
		if (this.maxHealth != null)
			if (this.maxHealth != vd.maxHealth) return false; 
		
		return true;
	}

	@Override
	public CreatureData parseFromEntity(Entity entity) {
		if (entity instanceof Zombie) {
			return new ZombieData(((Zombie)entity).isVillager(), ((Zombie)entity).isBaby(), ((Zombie)entity).getMaxHealth());
		} else {
			Log.logInfo("ZombieData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}

	@Override
	public CreatureData parseFromString(String state) {
		// state example: VILLAGER!BABY, BABY, BABY!NORMAL (order doesn't matter)
		Boolean adult = null;
		Boolean villager = null;
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
					else if (sub.equalsIgnoreCase("villager"))  villager = true;
					else if (sub.equalsIgnoreCase("normal"))    villager = false;
				}
			}
		}

		return new ZombieData(villager, adult, maxHealth);
	}
	
	public String toString() {
		String val = "";
		if (villager != null) {
			val += "!";
			val += villager?"VILLAGER":"NORMAL";
		}
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
