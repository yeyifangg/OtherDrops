package com.gmail.zariust.otherdrops.special;

import java.util.List;
import java.util.Properties;

import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;

/**
 * A plugin providing one or more events as an extension to OtherDrops.
 */
public abstract class SpecialResultHandler {
	Properties info;
	String version;

	/**
	 * Get a new event with the specified tag.
	 * @param name The event tag
	 * @return The new event, or null if the tag is not recognized
	 */
	public abstract SpecialResult getNewEvent(String name);

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
	 * The event plugin's node in the otherdrops-config.yml file. If it doesn't exist,
	 * it will be created.
	 * @return The configuration node.
	 */
	public ConfigurationNode getConfiguration() {
		return OtherDrops.plugin.config.getEventNode(this);
	}
	
	private String prefix() {
		return "[Event " + getName() + "] ";
	}
	
	/**
	 * Log an info message with default verbosity.
	 * @param msg The message to log.
	 */
	protected void logInfo(String msg) {
		OtherDrops.logInfo(prefix() + msg);
	}
	
	/**
	 * Log an info message with the specified verbosity.
	 * @param msg The message to log.
	 * @param verbosity The minimum verbosity for which it should appear.
	 */
	protected void logInfo(String msg, Verbosity verbosity) {
		OtherDrops.logInfo(prefix() + msg, verbosity);
	}
	
	/**
	 * Log a warning message with default verbosity.
	 * @param msg The message to log.
	 * @param msg
	 */
	protected void logWarning(String msg) {
		OtherDrops.logWarning(prefix() + msg);
	}
	
	/**
	 * Log a warning message with the specified verbosity.
	 * @param msg The message to log.
	 * @param verbosity The minimum verbosity for which it should appear.
	 */
	protected void logWarning(String msg, Verbosity verbosity) {
		OtherDrops.logWarning(prefix() + msg, verbosity);
	}
}
