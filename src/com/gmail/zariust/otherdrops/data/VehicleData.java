package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.common.CommonEntity;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class VehicleData implements Data {
	public enum VehicleState {EMPTY, PLAYER, OCCUPIED};
	CreatureType creature;
	// This flag has meaning only if creature is null
	// null = occupied by something, false = empty, true = occupied by player
	// null = may or may not be occupied
	VehicleState state;
	
	public VehicleData(Vehicle vehicle) {
		Entity passenger = vehicle.getPassenger();
		if(passenger instanceof Player) state = VehicleState.PLAYER;
		else creature = CommonEntity.getCreatureType(passenger);
		if(creature == null && state == null) state = VehicleState.EMPTY;
	}
	
	public VehicleData(VehicleState flag) {
		creature = null;
		state = flag;
	}

	public VehicleData(CreatureType type) {
		creature = type;
	}

	@Override
	public int getData() {
		if(creature == null) return state == null ? 0 : -state.ordinal();
		return creature.ordinal() + 1;
	}
	
	@Override
	public void setData(int d) {
		if(d > 0) creature = CreatureType.values()[d - 1];
		else {
			creature = null;
			if(d > -VehicleState.values().length)
				state = VehicleState.values()[-d];
			else state = VehicleState.EMPTY;
		}
	}
	
	@Override
	public boolean matches(Data d) {
		// TODO: This comparison is a bit convoluted; need to verify it really works
		if(!(d instanceof VehicleData)) return false;
		VehicleData vehicle = (VehicleData) d;
		if(creature == null) {
			// If creature and state are both null, it matches any vehicle data
			// (Though this should not occur in practice.)
			if(state == null) return true;
			switch(state) {
			case EMPTY: // If state is empty, it only matches empty
				return vehicle.creature == null && vehicle.state == VehicleState.EMPTY;
			case OCCUPIED: // If state is occupied, it matches anything except empty
				return vehicle.creature != null || vehicle.state != VehicleState.EMPTY;
			case PLAYER: // If state is player, it only matches player
				return vehicle.creature == null && vehicle.state == VehicleState.PLAYER;
			}
		}
		// Otherwise, must be the same creature
		return creature == vehicle.creature;
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat == Material.BOAT || mat == Material.MINECART)
			return creature == null ? (state == null ? state.toString() : "") : creature.toString();
		return "";
	}

	@Override
	public void setOn(Entity entity, Player witness) {
		Entity mob;
		if(creature == null) {
			if(state == VehicleState.EMPTY) return;
			mob = witness;
		} else mob = entity.getWorld().spawnCreature(entity.getLocation(), creature);
		entity.setPassenger(mob);
	}

	@Override // No vehicle has a block state, so nothing to do here.
	public void setOn(BlockState dummy) {}

	@SuppressWarnings("incomplete-switch")
	public static Data parse(Material mat, String state) {
		if(state == null || state.isEmpty()) return null;
		switch(mat) {
		case MINECART:
			CreatureType creature = CreatureType.fromName(state);
			if(creature != null) return new VehicleData(creature);
			// Fallthrough intentional
		case BOAT:
			try {
				VehicleState vs = VehicleState.valueOf(state);
				return new VehicleData(vs);
			} catch(IllegalArgumentException e) {}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		if(creature == null) {
			if(state == null) return 0;
			return state.hashCode();
		}
		return creature.hashCode();
	}
}
