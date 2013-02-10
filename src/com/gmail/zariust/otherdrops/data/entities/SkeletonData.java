package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

public class SkeletonData extends CreatureData {
	SkeletonType type = null; // null = wildcard
	Integer maxHealth = null;
	
	public SkeletonData(SkeletonType type, Integer maxHealth) {
		this.type = type;
		this.maxHealth = maxHealth;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Skeleton) {
			Skeleton z = (Skeleton)mob;
			if (type != null) z.setSkeletonType(type);
			if (maxHealth != null) {
				z.setMaxHealth(maxHealth);
				z.setHealth(maxHealth);
			}
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof SkeletonData)) return false;
		SkeletonData vd = (SkeletonData)d;

		if (this.type != null)
			if (this.type != vd.type) return false;
		if (this.maxHealth != null)
			if (this.maxHealth != vd.maxHealth) return false; 
		
		return true;
	}

	public static CreatureData parseFromEntity(Entity entity) {
		if (entity instanceof Skeleton) {
			return new SkeletonData(((Skeleton)entity).getSkeletonType(), ((Skeleton)entity).getMaxHealth());
		} else {
			Log.logInfo("SkeletonData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}

	public static CreatureData parseFromString(String state) {
		SkeletonType type = null;
		Integer maxHealth = null;

		if (!state.isEmpty() && !state.equals("0")) {
			String split[] = state.split("!");

			for (String sub : split) {
				if (sub.matches("[0-9]+")) { // need to check numbers before any .toLowerCase()
					maxHealth = Integer.valueOf(sub);
				} else {
					sub = sub.toLowerCase().replaceAll("[ -_]",  "");
					if (sub.equalsIgnoreCase("wither"))   type = SkeletonType.WITHER;
					else if (sub.equalsIgnoreCase("normal")) type = SkeletonType.NORMAL;
				}
			}
		}

		return new SkeletonData(type, maxHealth);
	}
	
	public String toString() {
		String val = "";
		if (type != null) {
			val += "!";
			val += type.name();
		}
		return val;
	}
	
	@Override
	public String get(Enum<?> creature) {
		if(creature instanceof EntityType) return this.toString();
		return "";
	}
	
}
