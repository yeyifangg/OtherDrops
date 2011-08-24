package com.gmail.zariust.bukkit.otherblocks.data;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SpawnerData implements Data {
	private CreatureType creature;

	public SpawnerData(BlockState state) {
		if(state instanceof CreatureSpawner)
			creature = ((CreatureSpawner)state).getCreatureType();
	}
	
	public SpawnerData(CreatureType type) {
		creature = type;
	}

	@Override
	public int getData() {
		return creature.ordinal() + 1;
	}
	
	@Override
	public void setData(int d) {
		creature = CreatureType.values()[d - 1];
	}
	
	@Override
	public boolean matches(Data d) {
		if(!(d instanceof SpawnerData)) return false;
		return creature == ((SpawnerData)d).creature;
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat == Material.MOB_SPAWNER) 
			return creature.toString();
		return "";
	}

	@Override
	public void setOn(BlockState state) {
		if(!(state instanceof CreatureSpawner)) {
			OtherBlocks.logWarning("Tried to change a spawner block, but no spawner block was found!");
			return;
		}
		((CreatureSpawner)state).setCreatureType(creature);
	}

	@Override // Spawners aren't entities, so nothing to do here.
	public void setOn(Entity entity, Player witness) {}

	public static Data parse(String state) {
		CreatureType type = CreatureType.fromName(state);
		if(type != null) return new SpawnerData(type);
		return new SimpleData();
	}
}
