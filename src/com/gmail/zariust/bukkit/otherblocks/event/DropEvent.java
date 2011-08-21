package com.gmail.zariust.bukkit.otherblocks.event;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;

public abstract class DropEvent {
	private String tag;
	private DropEventHandler handler;
	private List<String> usedArgs;
	
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
	
	protected void used(String arg) {
		usedArgs.add(arg);
	}
	
	@Override
	public String toString() {
		StringBuilder event = new StringBuilder();
		event.append(tag);
		if(usedArgs.size() == 0) return event.toString();
		event.append("@");
		event.append(usedArgs.get(0));
		if(usedArgs.size() == 1) return event.toString();
		for(String arg : usedArgs.subList(1, usedArgs.size()))
			event.append("/" + arg);
		return event.toString();
	}
	
	// TODO: Does this need more parameters? Should we pass the entire drop object?
	public abstract void executeAt(OccurredDrop event);
	
	public abstract void interpretArguments(String... args);

	public abstract boolean canRunFor(SimpleDrop drop);

	public abstract boolean canRunFor(OccurredDrop drop);
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
		if(result.isEmpty()) return null;
		return result;
	}
}
