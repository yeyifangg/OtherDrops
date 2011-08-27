package com.gmail.zariust.otherdrops.data;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Data {
	int getData();
	void setData(int d);
	boolean matches(Data d);
	String get(Enum<?> mat);
	void setOn(BlockState state);
	void setOn(Entity entity, Player witness);
}
