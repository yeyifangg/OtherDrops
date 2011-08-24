package com.gmail.zariust.bukkit.otherblocks.data;

import java.util.HashMap;
import java.util.Map;

import com.gmail.zariust.bukkit.common.CommonEntity;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class VehicleData implements Data {
	CreatureType creature;
	// This flag has meaning only if creature is null
	// null = occupied by something, false = empty, true = occupied by player
	Boolean player;
	private static Map<Boolean,String> mapping = new HashMap<Boolean,String>();
	
	static {
		mapping.put(null, "OCCUPIED");
		mapping.put(false, "EMPTY");
		mapping.put(true, "PLAYER");
	}
	
	public VehicleData(Vehicle vehicle) {
		Entity passenger = vehicle.getPassenger();
		if(passenger instanceof Player) player = true;
		else creature = CommonEntity.getCreatureType(passenger);
		if(creature == null && player == null) player = false;
	}
	
	public VehicleData(Boolean flag) {
		creature = null;
		player = flag;
	}

	public VehicleData(CreatureType type) {
		creature = type;
	}

	@Override
	public int getData() {
		if(creature == null) return player == null ? -2 : (player ? -1 : 0);
		return creature.ordinal() + 1;
	}
	
	@Override
	public void setData(int d) {
		if(d > 0) creature = CreatureType.values()[d - 1];
		else {
			creature = null;
			switch(d) {
			case 0: player = false; break;
			case -1: player = true; break;
			case -2: player = null; break;
			}
		}
	}
	
	@Override
	public boolean matches(Data d) {
		// TODO: This comparison is a bit convoluted; need to verify it really works
		if(!(d instanceof VehicleData)) return false;
		VehicleData vehicle = (VehicleData) d;
		if(creature == null && player == null)
			return vehicle.player != Boolean.FALSE;
		if(creature != vehicle.creature) return true;
		return player == vehicle.player;
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat == Material.BOAT || mat == Material.MINECART)
			return creature == null ? mapping.get(player) : creature.toString();
		return "";
	}

	@Override
	public void setOn(Entity entity, Player witness) {
		Entity mob;
		if(creature == null) {
			if(player == Boolean.FALSE) return;
			mob = witness;
		} else mob = entity.getWorld().spawnCreature(entity.getLocation(), creature);
		entity.setPassenger(mob);
	}

	@Override // No vehicle has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@SuppressWarnings("incomplete-switch")
	public static Data parse(Material mat, String state) {
		switch(mat) {
		case MINECART:
			CreatureType creature = CreatureType.fromName(state);
			if(creature != null) return new VehicleData(creature);
			// Fallthrough intentional
		case BOAT:
			if(state.equals("OCCUPIED")) return new VehicleData((Boolean)null);
			else if(state.equals("EMPTY")) return new VehicleData(false);
			else if(state.equals("PLAYER")) return new VehicleData(true);
		}
		return new VehicleData(false);
	}
}
