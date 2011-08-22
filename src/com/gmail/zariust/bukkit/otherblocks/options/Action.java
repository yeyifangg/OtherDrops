package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.HashMap;
import java.util.Map;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.ConfigurationNode;

public final class Action {
	public final static Action BREAK = new Action();
	public final static Action LEFT_CLICK = new Action();
	public final static Action RIGHT_CLICK = new Action();
	public final static Action LEAF_DECAY = new Action();
	private static Map<String,Action> actions = new HashMap<String,Action>();
	private static Map<String,Plugin> owners = new HashMap<String,Plugin>();
	
	static {
		actions.put("BREAK", BREAK);
		actions.put("LEFT_CLICK", LEFT_CLICK);
		actions.put("RIGHT_CLICK", RIGHT_CLICK);
		actions.put("LEAF_DECAY", LEAF_DECAY);
		owners.put("BREAK", OtherBlocks.plugin);
		owners.put("LEFT_CLICK", OtherBlocks.plugin);
		owners.put("RIGHT_CLICK", OtherBlocks.plugin);
		owners.put("LEAF_DECAY", OtherBlocks.plugin);
	}
	
	private Action() {}
	
	public static Action fromInteract(org.bukkit.event.block.Action action) {
		switch(action) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			return LEFT_CLICK;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			return RIGHT_CLICK;
		default:
			return null;
		}
	}
	
	public void register(Plugin plugin, String tag) {
		if(plugin instanceof OtherBlocks)
			throw new IllegalArgumentException("Use your own plugin for registering an action!");
		actions.put(tag, new Action());
		owners.put(tag, plugin);
	}
	
	public void unregister(Plugin plugin, String tag) {
		Plugin check = owners.get(tag);
		if(!check.getClass().equals(plugin.getClass()))
			throw new IllegalArgumentException("You didn't register that action!");
		owners.remove(tag);
		actions.remove(tag);
	}

	public static Action parseFrom(ConfigurationNode dropNode) {
		String action = dropNode.getString("action", "BREAK");
		return actions.get(action.toUpperCase());
	}
}
