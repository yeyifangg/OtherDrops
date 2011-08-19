package com.gmail.zariust.bukkit.otherblocks.options.event;

import java.util.List;
import java.util.Properties;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public abstract class DropEventHandler {
	Properties info;

	public abstract DropEvent getNewEvent(String name);

	public abstract void onLoad();

	public abstract List<String> getEvents();

	public abstract String getName();
	
	public Properties getInfo() {
		return info;
	}
	
	public ConfigurationNode getConfiguration() {
		return OtherBlocks.plugin.config.getEventNode(this);
	}
}
