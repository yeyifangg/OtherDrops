package com.gmail.zariust.bukkit.otherblocks.options.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;

public abstract class DropEvent {
	private String tag;
	private DropEventHandler handler;
	
	protected DropEvent(String name, DropEventHandler source) {
		tag = name;
		handler = source;
	}
	
	public String getTag() {
		return tag;
	}
	
	public DropEventHandler getHandler() {
		return handler;
	}
	
	// TODO: Does this need more parameters? Should we pass the entire drop object?
	public abstract void executeAt(Location location);
	
	public abstract void interpretArguments(String... args);

	public abstract boolean canRunFor(SimpleDrop drop);
	//LIGHTNING, EXPLOSION, TREE, FORCETREE, SHEAR, UNSHEAR, SHEARTOGGLE;

	public static List<DropEvent> parseFrom(ConfigurationNode node) {
		List<String> events = OtherBlocksConfig.getMaybeList(node, "event");
		if(events == null) return null;
		// There's a good reason for using LinkedList; changing it could break things
		List<DropEvent> result = new LinkedList<DropEvent>();
		for(String eventCall : events) {
			String[] split = eventCall.split("@");
			String name = split[0].toUpperCase();
			DropEventHandler handler = DropEventLoader.getHandlerFor(name);
			if(handler == null) {
				OtherBlocks.logWarning("Unknown event type " + name + "; skipping...");
				continue;
			}
			DropEvent event = handler.getNewEvent(name);
			if(split.length > 1) event.interpretArguments(split[1].split("/"));
			result.add(event);
		}
	}
}
