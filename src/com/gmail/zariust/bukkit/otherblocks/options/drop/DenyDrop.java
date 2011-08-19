package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

public class DenyDrop extends DropType {
	private Cancellable event;
	
	public DenyDrop(Cancellable evt) {
		super(DropCategory.DENY);
		event = evt;
	}

	public Cancellable getEvent() {
		return event;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		event.setCancelled(true);
	}
}
