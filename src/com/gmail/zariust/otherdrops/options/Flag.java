package com.gmail.zariust.otherdrops.options;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.ConfigurationNode;

/**
 * Represents a boolean flag which a drop can either satisfy or not satisfy.
 */
public abstract class Flag implements Comparable<Flag> {
	/**
	 * Indicates that no other drop can accompany this drop.
	 */
	public final static Flag UNIQUE = new Flag("UNIQUE") {
		@Override public void matches(OccurredDropEvent event, boolean state, final FlagState result) {
			if(state) {
				result.dropThis = true;
				result.continueDropping = false;
			}
		}
	};
	public final static class FlagState {
		public boolean dropThis = true;
		public boolean continueDropping = true;
	};
	// LinkedHashMap because I want to preserve order
	private static Map<String,Flag> flags = new LinkedHashMap<String,Flag>();
	private static int nextOrdinal = 0;
	private int ordinal;
	private String name;
	private Plugin pl;
	
	static {
		flags.put("UNIQUE", UNIQUE);
	}
	
	private Flag(String tag) {
		name = tag;
		ordinal = nextOrdinal;
		nextOrdinal++;
		pl = OtherDrops.plugin;
	}
	
	protected Flag(Plugin plugin, String tag) {
		this(tag);
		if(plugin == null || plugin instanceof OtherDrops)
			throw new IllegalArgumentException("Use your own plugin for registering a flag!");
		pl = plugin;
	}
	
	/**
	 * Register a new flag to your plugin.
	 * @param flag The flag to register.
	 */
	public static void register(Flag flag) {
		flags.put(flag.name, flag);
	}
	
	/**
	 * Unregister a previously registered flag.
	 * @param plugin The plugin that registered the action (preferably your plugin).
	 * @param flag The flag to unregister.
	 */
	public static void unregister(Plugin plugin, Flag flag) {
		if(!flag.pl.getClass().equals(plugin.getClass()))
			throw new IllegalArgumentException("You didn't register that flag!");
		flags.remove(flag.name);
	}

	// TODO: Return a list of flags
	public static Set<Flag> parseFrom(ConfigurationNode dropNode) {
		List<String> list = OtherDropsConfig.getMaybeList(dropNode, "flag", "flags");
		Set<Flag> set = new HashSet<Flag>();
		for(String flag : list) {
			Flag add = flags.get(flag.toUpperCase());
			if(add != null) set.add(add);
		}
		return set;
	}

	@Override
	public final int compareTo(Flag other) {
		return Integer.valueOf(ordinal).compareTo(other.ordinal);
	}
	
	@Override
	public final boolean equals(Object other) {
		if(!(other instanceof Flag)) return false;
		return ordinal == ((Flag)other).ordinal;
	}

	@Override
	public final int hashCode() {
		return ordinal;
	}
	
	@Override
	public final String toString() {
		return name;
	}

	/**
	 * Return a list of all valid flags.
	 * @return All actions.
	 */
	public static Flag[] values() {
		return flags.values().toArray(new Flag[0]);
	}
	
	/**
	 * Return a list of all valid flag names.
	 * @return All actions.
	 */
	public static Set<String> getValidFlags() {
		return flags.keySet();
	}
	
	/**
	 * Get a flag by name.
	 * @param key The flag tag name.
	 * @return The flag, or null if it does not exist.
	 */
	public static Flag valueOf(String key) {
		return flags.get(key);
	}
	
	/**
	 * Check if the flag applies to the given event. All registered flags will be checked for each event,
	 * which means that this is called regardless of whether the flag was set.
	 * @param event A drop event to check against.
	 * @param state Whether the flag is set on the event; typically you only do anything if this is
	 * true, but sometimes you also need to do something if it is false.
	 * @param result The result of the check, including whether to drop this drop and whether
	 * to continue processing further drops. This parameter should be declared final.
	 */
	public abstract void matches(OccurredDropEvent event, boolean state, final FlagState result);
}
