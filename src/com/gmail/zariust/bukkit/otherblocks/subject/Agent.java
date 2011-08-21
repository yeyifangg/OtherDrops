package com.gmail.zariust.bukkit.otherblocks.subject;

import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

import org.bukkit.Location;

public interface Agent {
	boolean matches(Agent other);

	ItemType getType();
	
	public void damage(int amount);

	public void damageTool(short amount);
	
	public void damageTool();

	Location getLocation();
}
