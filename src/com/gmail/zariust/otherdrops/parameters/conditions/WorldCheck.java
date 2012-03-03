package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class WorldCheck extends Condition {
	private Map<org.bukkit.World, Boolean> worlds;
	
	@Override
	public boolean check(OccurredEvent occurrence) {
		String worldName = occurrence.getWorld().getName();
		org.bukkit.World world = occurrence.getWorld();
		
		return CustomDrop.checkList(world, worlds);
	}

//	@Override
	public boolean parse(Object object) {
		if (object == null) return false;

		List <String> list = new ArrayList<String>();
		if(object instanceof List) list = (List)object;
		else list = Collections.singletonList(object.toString());

		return true;
	}	

}
