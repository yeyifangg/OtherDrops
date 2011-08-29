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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.bukkit.otherblocks;

import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.painting.PaintingBreakEvent;

import com.gmail.zariust.bukkit.common.*;

public class OtherBlocksEntityListener extends EntityListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksEntityListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
	    if (!OtherBlocksConfig.dropForCreatures) return;
	    OtherBlocks.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")", 5);

	    // Ignore if a player
	    //if(event.getEntity() instanceof Player) return;

	    // Check if the damager is a player - if so, weapon is the held tool
	    if(event instanceof EntityDamageByEntityEvent) {
	        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
	        if(e.getDamager() instanceof Player) {
	            Double rangeDouble = e.getDamager().getLocation().distance(event.getEntity().getLocation());
	            String range = String.valueOf(rangeDouble.intValue());
	            Player damager = (Player) e.getDamager();
	            parent.damagerList.put(event.getEntity(), damager.getItemInHand().getType().toString()+"@"+damager.getName()+"@"+range);
	            return;
	        } else if (e.getDamager() == null) {
	            // For some reason dispenser's return null for e.getDamager().  So this is probably a dispenser - what else can throw an arrow other than a player and skeleton? 
	            parent.damagerList.put(event.getEntity(), "DAMAGE_ENTITY_ATTACK@BLOCK_DISPENSER");
	            return;
	        } else {
	            CreatureType attacker = CommonEntity.getCreatureType(e.getDamager());
	            if(attacker != null) {
	                parent.damagerList.put(event.getEntity(), "DAMAGE_ENTITY_ATTACK@CREATURE_" + attacker.toString());
	                return;
	            }
	        }
	    }


		// Damager was not a person - switch through damage types
		switch(event.getCause()) {
		    case FIRE:
		    case FIRE_TICK:
		    case LAVA:
		        parent.damagerList.put(event.getEntity(), "DAMAGE_FIRE");
		        break;
		        
		    case ENTITY_ATTACK:
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
		    case CONTACT:
		    case DROWNING:
		    case FALL:
		    case SUFFOCATION:
		    case LIGHTNING:
		        parent.damagerList.put(event.getEntity(), "DAMAGE_" + event.getCause().toString());
		        break;
		        
		    case CUSTOM:
		    case VOID:
		    default:
		        parent.damagerList.remove(event.getEntity());
		        break;
		}
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (OtherBlocks.mobArenaHandler != null) {
			if (OtherBlocks.mobArenaHandler.inRunningRegion(event.getEntity().getLocation())) {
				return;
			}
		}

		if (!OtherBlocksConfig.dropForCreatures) return;
		// TODO: use get getLastDamageCause rather than checking on each getdamage?
		//parent.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+") last damagecause:"+event.getEntity().getLastDamageCause());
		OtherBlocks.logInfo("OnEntityDeath, before damagerList check (victim: "+event.getEntity().toString()+")", 4);

		// At the moment, we only track creatures killed by humans
		// commented out by Celtic
		//if(event.getEntity() instanceof Player) return;

		// If there's no damage record, ignore
		if(!parent.damagerList.containsKey(event.getEntity())) return;

		OtherBlocksDrops.checkDrops(event, parent);
		parent.damagerList.remove(event.getEntity());
	}

	@Override
	public void onPaintingBreak(PaintingBreakEvent event) {
	    // If there's no damage record, ignore
		// TOFIX:: paintings do not trigger "onEntityDamage"
		//if(!parent.damagerList.containsKey(event.getPainting())) return;
		
		parent.damagerList.remove(event.getPainting());
		OtherBlocksDrops.checkDrops(event, parent);
	}
}

