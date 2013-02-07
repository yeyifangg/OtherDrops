package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

public class CreeperData extends CreatureData {
	Creeper dummy; // used to represent main Entity class for this data object
	Boolean powered = null; // null = wildcard
	
	public CreeperData(Boolean powered) {
		this.powered = powered;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Creeper) {
			if (powered != null) 
				if (powered) {
					((Creeper)mob).setPowered(true);
				}
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d.getClass().equals(this))) return false;
		
		CreeperData vd = (CreeperData)d;
		
		if (this.powered != null)
			if (this.powered != vd.powered) return false; 
		
		return true;
	}

	@Override
	public CreatureData parseFromEntity(Entity entity) {
		if (entity == null) return null;
		if (dummy.getClass().equals(entity)) {
			return new CreeperData(dummy.getClass().cast(entity).isPowered());
		} else {
			Log.logInfo("CreeperData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}

	@Override
	public CreatureData parseFromString(String state) {
		Boolean powered = null;
		if (state.equalsIgnoreCase("powered")) powered = true;
		if (state.equalsIgnoreCase("unpowered")) powered = false;

		if (powered == null) return null;
		else return new CreeperData(powered);
	}

	public String toString() {
		String val = "";
		if (powered != null) {
			val += powered?"POWERED":"UNPOWERED";
		}
		return val;
	}
	
	@Override
	public String get(Enum<?> creature) {
		if(creature instanceof EntityType) return this.toString();
		return "";
	}
	
}
