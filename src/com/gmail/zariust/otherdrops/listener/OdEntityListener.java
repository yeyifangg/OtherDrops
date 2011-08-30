// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.painting.PaintingBreakEvent;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.EnvironmentAgent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ProjectileAgent;

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
		ProfilerEntry entry = new ProfilerEntry("INTERACTENTITY");
		OtherDrops.profiler.startProfiling(entry);

		OtherDrops.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")", EXTREME);

		// Check if the damager is a player - if so, weapon is the held tool
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager() instanceof Player) {
				Agent damager = new PlayerSubject((Player) e.getDamager());
				parent.damagerList.put(event.getEntity(), damager);
				return;
			} else if (e.getDamager() instanceof Projectile) {
				Agent damager = new ProjectileAgent((Projectile) e.getDamager()); 
				parent.damagerList.put(event.getEntity(), damager);
				return;
			} else if(e.getDamager() instanceof LivingEntity) {
				Agent attacker = new CreatureSubject((LivingEntity) e.getDamager());
				parent.damagerList.put(event.getEntity(), attacker);
				return;
			} else {
				// The only other one I can think of is lightning, which would be covered by the non-entity code
				// But just in case, log it.
				OtherDrops.logInfo("A " + event.getEntity().getClass().getSimpleName() + " was damaged by a "
						+ e.getDamager().getClass().getSimpleName(), HIGHEST);
			}
		}

		// Damager was not a person - switch through damage types
		DamageCause cause = event.getCause();
		if(cause == DamageCause.CUSTOM) return; // We don't handle custom damage
		// Dying by lava and by fire are close enough that they probably can't be distinguished
		// TODO: However, maybe that's not the case? Investigate?
		if(cause == DamageCause.FIRE_TICK || cause == DamageCause.LAVA)
			cause = DamageCause.FIRE;
		// Used to ignore void damage as well, but since events were added I can see some use for it.
		// For example, a lightning strike when someone falls off the bottom of the map.
		parent.damagerList.put(event.getEntity(), new EnvironmentAgent(cause));
		
		// Fire a left click event
		OccurredDropEvent drop = new OccurredDropEvent(event);
		OtherDrops.logInfo("EntityDamage occurance created. ("+drop.toString()+")",EXTREME);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (OtherDrops.mobArenaHandler != null) {
			if (OtherDrops.mobArenaHandler.inRunningRegion(event.getEntity().getLocation())) {
				return;
			}
		}
		
		if (!parent.config.dropForCreatures) return;
		// TODO: use get getLastDamageCause rather than checking on each getdamage?
		//parent.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+") last damagecause:"+event.getEntity().getLastDamageCause());
		OtherDrops.logInfo("OnEntityDeath, before damagerList check (victim: "+event.getEntity().toString()+")", HIGHEST);

		// If there's no damage record, ignore
		if(!parent.damagerList.containsKey(event.getEntity())) return;
		
		ProfilerEntry entry = new ProfilerEntry("ENTITYDEATH");
		OtherDrops.profiler.startProfiling(entry);

		OccurredDropEvent drop = new OccurredDropEvent(event);
		OtherDrops.logInfo("EntityDeath drop occurance created. ("+drop.toString()+")",HIGHEST);
		parent.performDrop(drop);
		
		parent.damagerList.remove(event.getEntity());
		OtherDrops.profiler.stopProfiling(entry);
	}

	@Override
	public void onPaintingBreak(PaintingBreakEvent event) {
		// TODO: Should we fire a left click before firing the painting break?
		ProfilerEntry entry = new ProfilerEntry("PAINTINGBREAK");
		OtherDrops.profiler.startProfiling(entry);
		OccurredDropEvent drop = new OccurredDropEvent(event);
		OtherDrops.logInfo("PaintingBreak drop occurance created. ("+drop.toString()+")",HIGHEST);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		// TODO: Why is this commented out?
		//if(!parent.config.dropForExplosions) return;

		// Disable certain types of drops temporarily since they can cause feedback loops
		// Note: This will disable ALL plugins that create explosions in the same way as the explosion event
		if (event.getEntity() == null) {
			OtherDrops.logInfo("EntityExplode - no entity found, skipping.");
			return; // skip recursive explosions, for now (explosion event has no entity) TODO: add an option?
		}
		if (OtherDrops.mobArenaHandler != null) {
			if (event.getEntity() != null) {
				if (OtherDrops.mobArenaHandler.inRunningRegion(event.getEntity().getLocation())) {
					return;
				}
			}
		}
		// Called to match blockbreak drops when tnt or creepers explode
		ProfilerEntry entry = new ProfilerEntry("EXPLODE");
		OtherDrops.profiler.startProfiling(entry);
		OtherDrops.logInfo("EntityExplode occurance detected - drop occurences will be created for each block.", HIGHEST);
		for(Block block : event.blockList()) {
			OccurredDropEvent drop = new OccurredDropEvent(event, block);
			parent.performDrop(drop);
		}
		OtherDrops.profiler.stopProfiling(entry);
	}
}

