package com.gmail.zariust.bukkit.otherblocks.data;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EffectData implements Data {
	
	@Override
	public int getData() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void setData(int d) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean matches(Data d) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String get(Material mat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override // No effect has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@Override // Effects are not entities, so nothing to do here.
	public void setOn(Entity entity, Player witness) {}
}
