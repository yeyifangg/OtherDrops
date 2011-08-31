package com.gmail.zariust.common;

import java.util.List;

import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public final class CommonPlugin {
	
	static public List<String> getRootKeys(JavaPlugin plugin) {
		List <String> keys; 
		try {
			keys = plugin.getConfiguration().getKeys(null);
		} 
		catch(NullPointerException ex) { return null; }
		return keys;
	}


	static public List<String> getConfigRootKeys(Configuration config) {
		List <String> keys; 
		try {
			keys = config.getKeys(null);
		} 
		catch(NullPointerException ex) { return null; }
		return keys;
	}
	
	static public Verbosity getConfigVerbosity(Configuration config) {
		String verb_string = config.getString("verbosity", "normal");
		if(verb_string.equalsIgnoreCase("low")) return Verbosity.LOW;
		else if(verb_string.equalsIgnoreCase("high")) return Verbosity.HIGH;
		else if(verb_string.equalsIgnoreCase("highest")) return Verbosity.HIGHEST;
		else if(verb_string.equalsIgnoreCase("extreme")) return Verbosity.EXTREME;
		else return Verbosity.NORMAL;
	}

	static public Priority getConfigPriority(Configuration config) {
		String priority_string = config.getString("priority", "lowest");
		if(priority_string.equalsIgnoreCase("low"))	 return Priority.Low;
		else if(priority_string.equalsIgnoreCase("normal")) return Priority.Normal;
		else if(priority_string.equalsIgnoreCase("high")) return Priority.High;
		else if(priority_string.equalsIgnoreCase("highest")) return Priority.Highest;
		else return Priority.Lowest;
	}
	
	static public <E extends Enum<E>> E enumValue(Class<E> clazz, String name) {
		try {
			return Enum.valueOf(clazz, name);
		} catch(IllegalArgumentException e) {}
		return null;
	}
}
