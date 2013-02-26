package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class OdSpawnListener implements Listener
{
	private OtherDrops parent;
	
	public OdSpawnListener(OtherDrops instance) {
		parent = instance;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.isCancelled()) return;
		Log.logInfo("SpawnEvent: before checks.  Spawned by "+event.getSpawnReason().toString(), Verbosity.EXTREME);

		// This listener should only be registered if "spawned" condition exists, so tag creature
		event.getEntity().setMetadata("CreatureSpawnedBy", new FixedMetadataValue(OtherDrops.plugin, event.getSpawnReason().toString()));
		
		// Only run OccurredEvent/performDrop if "action: SPAWN" trigger used
		if (OtherDropsConfig.dropForSpawnTrigger) {
			if (event.getSpawnReason().equals(SpawnReason.CUSTOM)) {
				if (OtherDropsConfig.spawnTriggerIgnoreCustom) {
					Log.logInfo("SpawnEvent: ignoring as custom drops not allowed (see config file).", Verbosity.HIGH);
					return;
				}
			}

			OccurredEvent drop = new OccurredEvent(event);
			parent.performDrop(drop);
		}
	}

}
