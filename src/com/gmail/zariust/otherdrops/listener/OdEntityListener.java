// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.painting.PaintingBreakEvent;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.DropCreateException;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.ExplosionAgent;

public class OdEntityListener extends EntityListener
{	
	private OtherDrops parent;
	
	public OdEntityListener(OtherDrops instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		if (!parent.config.dropForCreatures) return;
		OtherDrops.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")", EXTREME);

		// Check if the damager is a player - if so, weapon is the held tool
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager() instanceof Player) {
				// Fire a left click event
				ProfilerEntry entry = new ProfilerEntry("INTERACTENTITY");
				OtherDrops.profiler.startProfiling(entry);
				OccurredEvent drop = new OccurredEvent(event);
				OtherDrops.logInfo("EntityDamage occurance created. ("+drop.toString()+")",EXTREME);
				parent.performDrop(drop);
				OtherDrops.profiler.stopProfiling(entry);
			}
		}
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (!parent.config.dropForCreatures) return;
		// TODO: use get getLastDamageCause rather than checking on each getdamage?
		OtherDrops.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+")", HIGHEST);
		Entity entity = event.getEntity();

		// If there's no damage record, ignore
		if(entity.getLastDamageCause() == null) {
			OtherDrops.logWarning("OnEntityDeath: entity "+entity.toString()+" has no 'lastDamageCause'.", HIGH);
			return;
		}
		
		ProfilerEntry entry = new ProfilerEntry("ENTITYDEATH");
		OtherDrops.profiler.startProfiling(entry);

		OccurredEvent drop = new OccurredEvent(event);
		OtherDrops.logInfo("EntityDeath drop occurance created. ("+drop.toString()+")",HIGHEST);
		parent.performDrop(drop);
		
		OtherDrops.profiler.stopProfiling(entry);
	}

	@Override
	public void onPaintingBreak(PaintingBreakEvent event) {
		// TODO: Should we fire a left click before firing the painting break?
		ProfilerEntry entry = new ProfilerEntry("PAINTINGBREAK");
		OtherDrops.profiler.startProfiling(entry);
		OccurredEvent drop = new OccurredEvent(event);
		OtherDrops.logInfo("PaintingBreak drop occurance created. ("+drop.toString()+")",HIGHEST);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		// TODO: Why was this commented out?
		if(!parent.config.customDropsForExplosions) return;
		if (event.isCancelled()) return;

		// Disable certain types of drops temporarily since they can cause feedback loops
		// Note: This will disable ALL plugins that create explosions in the same way as the explosion event
		if (event.getEntity() == null) {
			OtherDrops.logInfo("EntityExplode - no entity found, skipping.", HIGHEST);
			return; // skip recursive explosions, for now (explosion event has no entity) TODO: add an option?
		}
		
		// TODO: add a config item to enable enderdragon explosions if people want to use it with v.low chance drops
		if (event.getEntity() instanceof EnderDragon) return; // Enderdragon explosion drops will lag out the server....
		
		OtherDrops.logInfo("Processing explosion...", HIGHEST);
		// Called to match blockbreak drops when tnt or creepers explode
		ProfilerEntry entry = new ProfilerEntry("EXPLODE");
		OtherDrops.profiler.startProfiling(entry);
		OtherDrops.logInfo("EntityExplode occurance detected - drop occurences will be created for each block.", HIGHEST);
		for(Block block : event.blockList()) {
			// one for the explosion event
			OccurredEvent drop = new OccurredEvent(event, block);
			parent.performDrop(drop);
			
			// one for the block break
			OccurredEvent drop2;
			try {
				drop2 = new OccurredEvent(block, Action.BREAK, new ExplosionAgent(event.getEntity()));
				parent.performDrop(drop2);
			} catch (DropCreateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		OtherDrops.profiler.stopProfiling(entry);
	}
}

