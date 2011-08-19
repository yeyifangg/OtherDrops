package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.Location;

public class DefaultDrop extends DropType {
	public DefaultDrop() {
		super(DropCategory.DEFAULT);
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		// Do nothing; TODO This probably won't work quite as expected.
	}
}
