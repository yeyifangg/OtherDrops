package com.gmail.zariust.otherdrops.event;

import java.util.List;
import java.util.Properties;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.otherdrops.OtherBlocks;

/**
 * A plugin providing one or more events as an extension to OtherBlocks.
 */
public abstract class DropEventHandler {
	Properties info;
	String version;

	/**
	 * Get a new event with the specified tag.
	 * @param name The event tag
	 * @return The new event, or null if the tag is not recognized
	 */
	public abstract DropEvent getNewEvent(String name);

	/**
	 * The place to do any initialization.
	 */
	public abstract void onLoad();

	/**
	 * Get a list of recognized tags; this is used to register the tags to your plugin.
	 * @return A list of tags.
	 */
	public abstract List<String> getEvents();

	/**
	 * The name of the event handler.
	 * @return An identifiable name.
	 */
	public abstract String getName();
	
	/**
	 * The version of your plugin.
	 * @return The version string, or "1.0" if not defined.
	 */
	public final String getVersion() {
		return version;
	}
	
	/**
	 * The event plugin info file (event.info); you can obtain arbitrary information from it if you wish.
	 * @return The Properties instance.
	 */
	public Properties getInfo() {
		return info;
	}
	
	/**
	 * The event plugin's node in the otherblocks.yml file. If it doesn't exist,
	 * it will be created.
	 * @return The configuration node.
	 */
	// TODO: Create if it doesn't exist
	public ConfigurationNode getConfiguration() {
		return OtherBlocks.plugin.config.getEventNode(this);
	}
	
	private String prefix() {
		return "|[Event " + getName() + "] ";
	}
	
	/**
	 * Log an info message with default verbosity.
	 * @param msg The message to log.
	 */
	protected void logInfo(String msg) {
		OtherBlocks.logInfo(prefix() + msg);
	}
	
	/**
	 * Log an info message with the specified verbosity.
	 * @param msg The message to log.
	 * @param verbosity The minimum verbosity for which it should appear. (5 is maximum)
	 */
	protected void logInfo(String msg, int verbosity) {
		OtherBlocks.logInfo(prefix() + msg, verbosity);
	}
	
	/**
	 * Log a warning message with default verbosity.
	 * @param msg The message to log.
	 * @param msg
	 */
	protected void logWarning(String msg) {
		OtherBlocks.logWarning(prefix() + msg);
	}
	
	/**
	 * Log a warning message with the specified verbosity.
	 * @param msg The message to log.
	 * @param verbosity The minimum verbosity for which it should appear. (5 is maximum)
	 */
	protected void logWarning(String msg, int verbosity) {
		OtherBlocks.logWarning(prefix() + msg, verbosity);
	}
}
