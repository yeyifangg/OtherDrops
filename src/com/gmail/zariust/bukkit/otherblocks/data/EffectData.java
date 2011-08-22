package com.gmail.zariust.bukkit.otherblocks.data;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EffectData implements Data {
	int data;
	
	public EffectData(Material mat) { // BLOCK_BREAK effect
		data = mat.getId();
	}

	public EffectData(BlockFace face) { // SMOKE effect
		switch(face) {
		case EAST: data = 3; break;
		case NORTH: data = 7; break;
		case NORTH_EAST: data = 6; break;
		case NORTH_WEST: data = 8; break;
		case SOUTH: data = 1; break;
		case SOUTH_EAST: data = 0; break;
		case SOUTH_WEST: data = 2; break;
		case UP: data = 4; break;
		case WEST: data = 5; break;
		default: data = 4;
		}
	}

	@Override
	public int getData() {
		return data;
	}
	
	@Override
	public void setData(int d) {
		data = d;
	}
	
	@Override
	public boolean matches(Data d) {
		return data == d.getData();
	}
	
	@Override
	public String get(Material mat) {
		// TODO: Um what can I even do here? Fortunately I don't think it'll ever be called
		return null;
	}

	@Override // No effect has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@Override // Effects are not entities, so nothing to do here.
	public void setOn(Entity entity, Player witness) {}
}
