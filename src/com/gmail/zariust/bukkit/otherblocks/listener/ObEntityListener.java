// OtherBlocks - a Bukkit plugin
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

package com.gmail.zariust.bukkit.otherblocks.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.painting.PaintingBreakEvent;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.ProfilerEntry;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.subject.Agent;
import com.gmail.zariust.bukkit.otherblocks.subject.CreatureSubject;
import com.gmail.zariust.bukkit.otherblocks.subject.EnvironmentAgent;
import com.gmail.zariust.bukkit.otherblocks.subject.PlayerSubject;
import com.gmail.zariust.bukkit.otherblocks.subject.ProjectileAgent;

public class ObEntityListener extends EntityListener
{	
	private OtherBlocks parent;
	
	public ObEntityListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		//if (!parent.config.dropForCreatures) return; //TODO: fix me
		ProfilerEntry entry = new ProfilerEntry("INTERACTENTITY");
		OtherBlocks.profiler.startProfiling(entry);

		OtherBlocks.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")", 5);

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
				OtherBlocks.logInfo("A " + event.getEntity().getClass().getSimpleName() + " was damaged by a "
						+ e.getDamager().getClass().getSimpleName(), 4);
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
		OccurredDrop drop = new OccurredDrop(event);
		OtherBlocks.logInfo("EntityDamage occurance created. ("+drop.toString()+")",4);
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		//if (!parent.config.dropForCreatures) return; // TODO: fix this - not set properly yet
		// TODO: use get getLastDamageCause rather than checking on each getdamage?
		//parent.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+") last damagecause:"+event.getEntity().getLastDamageCause());
		OtherBlocks.logInfo("OnEntityDeath, before damagerList check (victim: "+event.getEntity().toString()+")", 4);

		// If there's no damage record, ignore
		if(!parent.damagerList.containsKey(event.getEntity())) return;
		
		ProfilerEntry entry = new ProfilerEntry("ENTITYDEATH");
		OtherBlocks.profiler.startProfiling(entry);

		OccurredDrop drop = new OccurredDrop(event);
		OtherBlocks.logInfo("EntityDeath drop occurance created. ("+drop.toString()+")",4);
		parent.performDrop(drop);
		
		parent.damagerList.remove(event.getEntity());
		OtherBlocks.profiler.stopProfiling(entry);
	}

	@Override
	public void onPaintingBreak(PaintingBreakEvent event) {
		// TODO: Should we fire a left click before firing the painting break?
		ProfilerEntry entry = new ProfilerEntry("PAINTINGBREAK");
		OtherBlocks.profiler.startProfiling(entry);
		OccurredDrop drop = new OccurredDrop(event);
		OtherBlocks.logInfo("PaintingBreak drop occurance created. ("+drop.toString()+")",4);
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		ProfilerEntry entry = new ProfilerEntry("EXPLODE");
		OtherBlocks.profiler.startProfiling(entry);
		OtherBlocks.logInfo("EntityExplode occurance detected - drop occurances will be created for each block.", 4);
		for(Block block : event.blockList()) {
			OccurredDrop drop = new OccurredDrop(event, block);
			parent.performDrop(drop);
		}
		OtherBlocks.profiler.stopProfiling(entry);
	}
}

