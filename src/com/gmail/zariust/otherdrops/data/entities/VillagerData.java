package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

public class VillagerData extends CreatureData {
	Profession prof = null; // null = wildcard
	Boolean adult = null;
	
	public VillagerData(Profession prof, Boolean adult) {
		this.prof = prof;
		this.adult = adult;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Villager) {
			if (prof != null) ((Villager)mob).setProfession(prof);
			if (adult != null) if (!adult) ((Villager)mob).setBaby();
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof VillagerData)) return false;
		
		VillagerData vd = (VillagerData)d;
		
		if (this.prof != null)
			if (this.prof != vd.prof) return false;
		if (this.adult != null)
			if (this.adult != vd.adult) return false; 
		
		return true;
	}

	public static CreatureData parseFromEntity(Entity entity) {
		if (entity instanceof Villager) {
			return new VillagerData(((Villager)entity).getProfession(), ((Villager)entity).isAdult());
		} else {
			Log.logInfo("VillagerData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}

	public static CreatureData parseFromString(String state) {
		// state example: BLACK_CAT!BABY!WILD, or TAME!REDCAT!ADULT (order doesn't matter)
		Boolean adult = null;
		Profession thisProf = null;

		if (!state.isEmpty() && !state.equals("0")) {
			String split[] = state.split("!");

			for (String sub : split) {
				sub = sub.toLowerCase().replaceAll("[ -_]",  "");
				if (sub.equalsIgnoreCase("adult")) adult = true;
				else if (sub.equalsIgnoreCase("baby"))  adult = false;
				else {
					// aliases
					//if (sub.equals("ocelot")) sub = "wildocelot";

					// loop through types looking for match
					for (Profession type : Profession.values()) {
						if (sub.equals(type.name().toLowerCase().replaceAll("[ -_]", "")))
							thisProf = type;
					}								
					if (thisProf == null) Log.logInfo("VillagerData: type not found ("+sub+")");
				}
			}
		}

		return new VillagerData(thisProf, adult);
	}
	
	public String toString() {
		String val = "";
		if (prof != null) val += prof.toString();
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
