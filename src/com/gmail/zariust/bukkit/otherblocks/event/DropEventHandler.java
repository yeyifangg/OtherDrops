package com.gmail.zariust.bukkit.otherblocks.event;

import java.util.List;
import java.util.Properties;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public abstract class DropEventHandler {
	protected Properties info;
	private String version;

	public abstract DropEvent getNewEvent(String name);

	public abstract void onLoad();

	public abstract List<String> getEvents();

	public abstract String getName();
	
	public String getVersion() {
		return version;
	}
	
	protected void setVersion(String vers) {
		version = vers;
	}
	
	public Properties getInfo() {
		return info;
	}
	
	public ConfigurationNode getConfiguration() {
		return OtherBlocks.plugin.config.getEventNode(this);
	}
	
	private String prefix() {
		return "|[Event " + getName() + "] ";
	}
	
	protected void logInfo(String msg) {
		OtherBlocks.logInfo(prefix() + msg);
	}
	
	protected void logInfo(String msg, int verbosity) {
		OtherBlocks.logInfo(prefix() + msg, verbosity);
	}
	
	protected void logWarning(String msg) {
		OtherBlocks.logWarning(prefix() + msg);
	}
	
	protected void logWarning(String msg, int verbosity) {
		OtherBlocks.logWarning(prefix() + msg, verbosity);
	}
}
