package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.event.DropEventHandler;

public class TreeEvents extends DropEventHandler {
	private OtherBlocks otherblocks;
	public static boolean forceOnTileEntities;
	
	public TreeEvents(OtherBlocks plugin) {
		otherblocks = plugin;
	}
	
	@Override
	public DropEvent getNewEvent(String name) {
		if(name.equalsIgnoreCase("TREE")) return new TreeEvent(this, false);
		else if(name.equalsIgnoreCase("FORCETREE")) return new TreeEvent(this, true);
		return null;
	}
	
	@Override
	public void onLoad() {
		setVersion(info.getProperty("version"));
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
