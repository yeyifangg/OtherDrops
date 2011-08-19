package com.gmail.zariust.bukkit.otherblocks.options.target;

import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public interface Target {
	public ItemType getType();

	public abstract boolean overrideOn100Percent();

	public boolean matches(Target block);
}
