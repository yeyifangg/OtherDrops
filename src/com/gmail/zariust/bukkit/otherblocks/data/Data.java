package com.gmail.zariust.bukkit.otherblocks.data;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Data {
	int getData();
	void setData(int d);
	boolean matches(Data d);
	String get(Material mat);
	void setOn(BlockState state);
	void setOn(Entity entity, Player witness);
}
