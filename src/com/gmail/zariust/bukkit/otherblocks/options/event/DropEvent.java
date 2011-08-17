package com.gmail.zariust.bukkit.otherblocks.options.event;

import org.bukkit.Location;

public abstract class DropEvent {
	private String tag;
	
	public String getTag() {
		return tag;
	}
	
	public abstract void executeAt(Location location);
	
	public abstract void interpretArguments(String... args);
	//LIGHTNING, EXPLOSION, TREE, FORCETREE, SHEAR, UNSHEAR, SHEARTOGGLE;
}
