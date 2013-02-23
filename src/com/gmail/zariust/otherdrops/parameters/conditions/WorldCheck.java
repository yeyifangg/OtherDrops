package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class WorldCheck extends Condition {
	private Map<org.bukkit.World, Boolean> worlds;
	
	public WorldCheck(List<String> list) {
		this.worlds = null;
	}

	@Override
	public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
		String worldName = occurrence.getWorld().getName();
		org.bukkit.World world = occurrence.getWorld();
		
		return CustomDrop.checkList(world, worlds);
	}

//	@Override
	public List<Condition> parseInstance(Object object) {
		if (object == null) return null;

		List <String> list = new ArrayList<String>();
		if(object instanceof List) list = (List)object;
		else list = Collections.singletonList(object.toString());

		List<Condition> conditionList = new ArrayList<Condition>();
		conditionList.add(new WorldCheck(list));
		return conditionList;
	}

	protected static List<Condition> parseInstance(ConfigurationNode node) {
		// TODO Auto-generated method stub
		return null;
	}	

}
