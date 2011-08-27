package com.gmail.zariust.obevents;

import java.util.Arrays;
import java.util.List;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.otherdrops.event.DropEvent;
import com.gmail.zariust.otherdrops.event.DropEventHandler;

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
		ConfigurationNode configNode = getConfiguration();
		forceOnTileEntities = (configNode == null) ? false : configNode.getBoolean("force-tile-entities", false);
		logInfo("Trees v" + getVersion() + " loaded.");
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
