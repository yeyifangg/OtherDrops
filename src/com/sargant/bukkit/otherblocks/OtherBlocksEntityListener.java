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

package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

import com.gmail.zarius.common.*;

public class OtherBlocksEntityListener extends EntityListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksEntityListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (parent.verbosity >= 5) {
			parent.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")");
		}
	    // Ignore if a player
	    //if(event.getEntity() instanceof Player) return;
		
	    // Check if the damager is a player - if so, weapon is the held tool
		if(event instanceof EntityDamageByEntityEvent) {
		    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		    if(e.getDamager() instanceof Player) {
		        Player damager = (Player) e.getDamager();
		        parent.damagerList.put(event.getEntity(), damager.getItemInHand().getType().toString()+"@"+damager.getName());
		        return;
		    } else {
		        CreatureType attacker = CommonEntity.getCreatureType(e.getDamager());
		        if(attacker != null) {
		            parent.damagerList.put(event.getEntity(), "CREATURE_" + attacker.toString());
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

		// At the moment, we only track creatures killed by humans
		// commented out by Celtic
		//if(event.getEntity() instanceof Player) return;

		// If there's no damage record, ignore
		if(!parent.damagerList.containsKey(event.getEntity())) return;

		String weapon = parent.damagerList.get(event.getEntity());
		Entity victim = event.getEntity();
		CreatureType victimType = CommonEntity.getCreatureType(victim);

		parent.damagerList.remove(event.getEntity());


		Player player = getPlayerFromWeapon(weapon, victim.getWorld());						
		if (weapon.contains("@"))
			weapon = weapon.split("@")[0];

		boolean otherblocksActive = true;
		if (parent.permissionsPlugin != null && player != null) {
			if (!(parent.permissionHandler.has(player, "otherblocks.active"))) {
				otherblocksActive = false;
			}
		}

		if (otherblocksActive) {

			// If victimType == null, the mob type has not been recognized (new, probably)
			// Stop here and do not attempt to process
			String victimTypeName = "";

			if (event.getEntity() instanceof Player) {
				Player victimPlayer = (Player)event.getEntity();
				victimTypeName = victimPlayer.getName();
			} else if(victimType == null) {
				return;
			} else {
				victimTypeName = victimType.toString();
			}

			Location location = victim.getLocation();
			List<OtherBlocksContainer> drops = new ArrayList<OtherBlocksContainer>();
			boolean doDefaultDrop = false;
			Short dataVal = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;

			// special case for pigs@saddled, wolf@tamed & creeper@
			if(victimTypeName == "PIG") {
				// @UNSADDLED=0, @SADDLED=1
				Pig pig = (Pig)victim;
				dataVal = (pig.hasSaddle()) ? (short)1 : (short)0;
			} else if(victimTypeName == "WOLF") {
				// @NEUTRAL=0, @TAME=1, @ANGRY=2 
				Wolf wolf = (Wolf)victim;
				dataVal = (wolf.isTamed()) ? (short)1 : (short)0;
				if (wolf.isAngry()) {
					dataVal = (short)2;
				}
			} else if(victimTypeName == "CREEPER") {
				// @UNPOWERED=0, @POWERED=1
				Creeper creeper = (Creeper)victim;
				dataVal = (creeper.isPowered()) ? (short)1 : (short)0;
			}

			if (parent.verbosity >= 3) {
				parent.logInfo("OnEntityDeath: "+event.getEntity().toString()+"@"+dataVal+", by "+weapon+"@"+player+" in "+victim.getWorld().getName()+")");
			}

			boolean successfulComparison = false;
			Integer maxAttackerDamage = 0;
			for(OtherBlocksContainer obc : parent.transformList) {
		
				if(!obc.compareTo(
						victim, 
						dataVal,
						weapon,
						victim.getWorld(),
						player,
						parent.permissionHandler)) {

					continue;
				}

				// Check probability is great than the RNG
				if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

				if(obc.dropped.equalsIgnoreCase("DEFAULT")) {
					doDefaultDrop = true;
				} else {
					drops.add(obc);
				}
			}

			// Now do the drops
			if(drops.size() > 0 && doDefaultDrop == false) event.getDrops().clear();
			for(OtherBlocksContainer obc : drops) OtherBlocks.performDrop(location, obc, player);
		}
	}

	Player getPlayerFromWeapon(String weapon, World world) {
		if (weapon == null || world == null)
			return null;
		
		String playerName = "";
		Player player = null;
		if (weapon.contains("@")) {
			String[] weaponList = weapon.split("@");
			weapon = weaponList[0];
			playerName = weaponList[1];
			List<Player> players = world.getPlayers();

			for(Player loopPlayer : world.getPlayers()) {
				if(loopPlayer == null) {
					break;
				} else {
					if(loopPlayer.getName().equalsIgnoreCase(playerName)) {
						player = loopPlayer;
						break;
					}
				}
			}
		}
		return player;
	}

	@Override
	public void onPaintingBreak(PaintingBreakEvent event) {
	    // If there's no damage record, ignore
		// TOFIX:: paintings do not trigger "onEntityDamage"
		//if(!parent.damagerList.containsKey(event.getPainting())) return;
		// TOFIX:: paintings drop item and painting even on 100% item drop
		parent.damagerList.remove(event.getPainting());
		if(event instanceof PaintingBreakByEntityEvent) {
			PaintingBreakByEntityEvent e = (PaintingBreakByEntityEvent) event;
			if(e.getRemover() instanceof Player) {
				Player damager = (Player) e.getRemover();
				parent.damagerList.put(event.getPainting(), damager.getItemInHand().getType().toString()+"@"+damager.getName());
			} else {
				CreatureType attacker = CommonEntity.getCreatureType(e.getRemover());
				if(attacker != null) {
					parent.damagerList.put(event.getPainting(), "CREATURE_" + attacker.toString());
				}
			}
		} else {

			// Damager was not a person - switch through damage types
			switch(event.getCause()) {
			case WORLD:
				parent.damagerList.put(event.getPainting(), "DAMAGE_WORLD");
				break;
			default:
				parent.damagerList.remove(event.getPainting());
				break;
			}
		}
		String weapon = parent.damagerList.get(event.getPainting());
		Entity victim = event.getPainting();

		Player player = getPlayerFromWeapon(weapon, victim.getWorld());					
		if (weapon.contains("@"))
			weapon = weapon.split("@")[0];
		
		boolean otherblocksActive = true;
		if (parent.permissionsPlugin != null && player != null) {
			if (!(parent.permissionHandler.has(player, "otherblocks.active"))) {
				otherblocksActive = false;
			}
		}

		if (otherblocksActive) {


			Location location = victim.getLocation();
			List<OtherBlocksContainer> drops = new ArrayList<OtherBlocksContainer>();
			boolean doDefaultDrop = false;

			for(OtherBlocksContainer obc : parent.transformList) {

				Short dataVal = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;

				if(!obc.compareTo(
						"PAINTING", 
						dataVal,
						weapon,
						victim.getWorld(),
						player,
						parent.permissionHandler)) {

					continue;
				}
				
				// Check probability is great than the RNG
				if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

				if(obc.dropped.equalsIgnoreCase("DEFAULT")) {
					doDefaultDrop = true;
				} else {
					drops.add(obc);
				}
			}

			// Now do the drops
			for(OtherBlocksContainer obc : drops) OtherBlocks.performDrop(location, obc, player);
			//if(doDefaultDrop) location.getWorld().dropItemNaturally(location, new ItemStack(Material.PAINTING, 1));
		}
	}
}

