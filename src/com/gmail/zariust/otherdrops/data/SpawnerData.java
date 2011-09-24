package com.gmail.zariust.otherdrops.data;

import java.util.Arrays;

import com.gmail.zariust.otherdrops.OtherDrops;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.CreatureType;
import static org.bukkit.entity.CreatureType.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SpawnerData implements Data {
	private static CreatureType[] all = {
		PIG, CHICKEN, COW, SHEEP, SQUID, CREEPER, GHAST, PIG_ZOMBIE, SKELETON, SPIDER, ZOMBIE, SLIME, MONSTER, GIANT,
		WOLF, CAVE_SPIDER, ENDERMAN, SILVERFISH
	};
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
		return Arrays.asList(all).indexOf(creature);
	}
	
	@Override
	public void setData(int d) {
		if(d <= all.length) creature = all[d];
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
			OtherDrops.logWarning("Tried to change a spawner block, but no spawner block was found!");
			return;
		}
		((CreatureSpawner)state).setCreatureType(creature);
	}

	@Override // Spawners aren't entities, so nothing to do here.
	public void setOn(Entity entity, Player witness) {}

	public static Data parse(String state) {
		CreatureType type = CreatureType.fromName(state);
		if(type != null) return new SpawnerData(type);
		return null;
	}
}
