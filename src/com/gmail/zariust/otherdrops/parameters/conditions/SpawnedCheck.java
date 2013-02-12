package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;

public class SpawnedCheck extends Condition {

	String name = "SpawnedCheck";
	private String spawnedBy;
	
	public SpawnedCheck(String loreName) {
		this.spawnedBy = loreName;
	}

	@Override
	public boolean checkInstance(OccurredEvent occurrence) {
		Log.logInfo("Checking for spawnedby condition... ("+spawnedBy+")", Verbosity.HIGHEST);
		Entity entity = null;
		
		if (occurrence.getRealEvent() instanceof EntityDeathEvent) {
			EntityDeathEvent edEvent = (EntityDeathEvent)occurrence.getRealEvent();
			entity = edEvent.getEntity();
		} else if (occurrence.getRealEvent() instanceof EntityDamageEvent) {
			EntityDamageEvent edEvent = (EntityDamageEvent)occurrence.getRealEvent();
			entity = edEvent.getEntity();
		}
		
		if (entity != null) {
			String spawnReason = "";
			if (entity.getMetadata("CreatureSpawnedBy").size() > 0)
				spawnReason = (String)entity.getMetadata("CreatureSpawnedBy").get(0).value();
			
			Log.logInfo("SpawnedCheck - checking: "+spawnedBy+" vs actual: "+spawnReason, Verbosity.HIGHEST);
			if (spawnedBy.equalsIgnoreCase(spawnReason))
				return true;
		}

		return false;
		
	}

	public static List<Condition> parse(ConfigurationNode node) {
		String value = node.getString("spawned");
		if (value == null) {
			value = node.getString("spawnedby");
		}
		if (value == null) {
			value = node.getString("spawn");
			if (value == null) return null;
		}

		OtherDropsConfig.dropForSpawned = true;
		
		List<Condition> conditionList = new ArrayList<Condition>();
		conditionList.add(new SpawnedCheck(value));
		Log.logInfo("check spawned");
		return conditionList;
	}	

	

}
