package com.gmail.zariust.bukkit.otherblocks.options.target;

import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public interface Target {
	ItemType getType();

	abstract boolean overrideOn100Percent();

	boolean matches(Target block);
	
	List<Target> canMatch();

	String getKey();
}
