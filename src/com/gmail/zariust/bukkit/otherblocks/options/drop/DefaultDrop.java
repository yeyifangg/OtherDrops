package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DefaultDrop extends DropType {
	public DefaultDrop() {
		super(DropCategory.DEFAULT);
	}

	@Override
	protected void performDrop(Location where, Player recipient, boolean naturally, boolean spread) {
		// Do nothing; TODO This probably won't work quite as expected.
	}
}
