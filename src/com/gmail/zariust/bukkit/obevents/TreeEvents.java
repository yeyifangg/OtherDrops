package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEventHandler;

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
