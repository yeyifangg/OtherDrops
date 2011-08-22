package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.event.DropEventHandler;

public class TreeEvents extends DropEventHandler {
	public static boolean forceOnTileEntities;
	
	@Override
	public DropEvent getNewEvent(String name) {
		if(name.equalsIgnoreCase("TREE")) return new TreeEvent(this, false);
		else if(name.equalsIgnoreCase("FORCETREE")) return new TreeEvent(this, true);
		return null;
	}
	
	@Override
	public void onLoad() {
		logInfo("Trees v" + getVersion() + " loaded.");
		forceOnTileEntities = getConfiguration().getBoolean("force-tile-entities", false);
	}
	
	@Override
	public List<String> getEvents() {
		return Arrays.asList("TREE", "FORCETREE");
	}
	
	@Override
	public String getName() {
		return "Trees";
	}
	
}
