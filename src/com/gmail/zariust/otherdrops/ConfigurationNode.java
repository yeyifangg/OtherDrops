// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
//
// This file released under Evil Software License v1.1
// <http://fredrikvold.info/ESL.htm>

package com.gmail.zariust.otherdrops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigurationNode {

	Map<String, ?> nodeMap;
	
	public ConfigurationNode(ConfigurationSection configurationSection) {
		// TODO Auto-generated constructor stub
	}

	// Example input: {drop=SULPHUR, chance=100, message=[Boom!]}
	public ConfigurationNode(Map<?, ?> map) {
		// TODO Auto-generated constructor stub
		nodeMap = (Map<String, ?>) map;
	}

	

	/** Parses a list of maps from the YamlConfig into "fake" ConfigurationNode list. 
	 *  Example input: [{drop=SULPHUR, chance=100, message=[Boom!]}, {tool=ANY_SPADE, action=LEFT_CLICK, drop=EGG}]
	 * 
	 * @param mapList from YamlConfiguration.getConfigurationSection().getMapList()
	 * @return a list of ConfigurationNode's
	 */
	public static List<ConfigurationNode> parse(List<Map<?, ?>> mapList) {
		//OtherDrops.logInfo(mapList.toString());
		List<ConfigurationNode> nodeList = new ArrayList<ConfigurationNode>();
		
		for (Map<?, ?> map : mapList)
			nodeList.add(new ConfigurationNode(map));
		
		return nodeList;
	}

	
	
	
	
	
	public List<String> getKeys() {
		if (nodeMap == null) return null;
				
		List<String> stringList = new ArrayList<String>();
		stringList.addAll(nodeMap.keySet());
		return stringList;
	}

	public String getString(String string) {
		if (nodeMap == null) return null;
		if (nodeMap.get(string) instanceof String) return (String) nodeMap.get(string);
		return null;
	}

	public List<String> getStringList(String key) {
		if (nodeMap == null) return null;
		if (nodeMap.get(key) instanceof List<?>) 
			if (((List<?>)nodeMap.get(key)).get(0) instanceof String)
				return (List<String>) nodeMap.get(key);
		
		return null;
	}

	// get property
	public Object get(String key) {
		if (nodeMap == null) return null;
		return nodeMap.get(key);
	}

	public String getString(String key, String defaultVal) {
		if (nodeMap == null) return null;
		if (nodeMap.get(key) instanceof String) return (String) nodeMap.get(key);
		return defaultVal;
	}

	
	// example:
	// [{dropgroup=fishcaught, action=FISH_CAUGHT, drops=[{drop=IRON_HELM@50, message=Bonus!You found an iron helmet - a little rusty but still ok., 
	// chance=90%, exclusive=1}, {drop={DIAMOND=null, GOLD_ORE=null, OBSIDIAN=null}, chance=90%, exclusive=1, message=Woo, you hooked a precious stone!}]}]
	public List<ConfigurationNode> getNodeList(String key, Object defaultVal) {
		if (nodeMap == null) return null;
		if (nodeMap.get(key) instanceof List<?>) 
			if (((List<?>)nodeMap.get(key)).get(0) instanceof Map) {
				List<ConfigurationNode> nodeList = new ArrayList<ConfigurationNode>();
				
				List<Map<?, ?>> mapList = (List<Map<?, ?>>) nodeMap.get(key);
				for (Map<?, ?> map : mapList)
					nodeList.add(new ConfigurationNode(map));
				
				return nodeList;
			}
		
		return null;
	}
	

}
