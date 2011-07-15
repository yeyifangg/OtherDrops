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
            parent.logInfo("OnEntityDamage (victim: "+event.getEntity().toString()+")", 5);
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

		// TODO: use get getLastDamageCause rather than checking on each getdamage?
		//parent.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+") last damagecause:"+event.getEntity().getLastDamageCause());
		parent.logInfo("OnEntityDeath, before checks (victim: "+event.getEntity().toString()+")", 4);

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
			String eventTarget = "";
			
			if (event.getEntity() instanceof Player) {
				Player victimPlayer = (Player)event.getEntity();
				victimTypeName = victimPlayer.getName();
				eventTarget = "PLAYER";
			} else if(victimType == null) {
				return;
			} else {
				victimTypeName = victimType.toString();
				eventTarget = "CREATURE_"+CommonEntity.getCreatureType(event.getEntity()).toString();
			}

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

                        parent.logInfo("OnEntityDeath: "+event.getEntity().toString()+"@"+dataVal+", by "+weapon+"@"+player+" in "+victim.getWorld().getName()+")", 3);

			Location location = victim.getLocation();
			boolean playerNoDrop = false;
			Integer maxAttackerDamage = 0;
			boolean doDefaultDrop = false;
			boolean denyBreak = false;
			boolean doDrop = true;
			String exclusive = null;

			//TODO: properly support creatures by integer value (for new itemcraft creatures)
			List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();

			parent.logInfo("ENTITYDEATH("+victimTypeName+"): before check.", 3);

			// grab the relevant collection of dropgroups
			OBContainer_DropGroups dropGroups = parent.config.blocksHash.get(eventTarget);

			// loop through dropgroups
			if (dropGroups == null) {
				parent.logWarning("ENTITYDEATH("+victimTypeName+"): warning - dropGroups is null!", 3);
				return;
			}
			for (OBContainer_Drops dropGroup : dropGroups.list) {
				if(!dropGroup.compareTo(
						victim, 
						dataVal,
						weapon,
						victim.getWorld(),
						player,
						parent.permissionHandler)) {

					continue;
				}


				// Loop through drops
				for (OB_Drop drop : dropGroup.list) {
					parent.logInfo("ENTITYDEATH("+victimTypeName+"): Before compareto", 3);

					if(!drop.compareTo(
							victim, 
							dataVal,
							weapon,
							victim.getWorld(),
							player,
							parent.permissionHandler)) {

						continue;
					}
					
					// Check probability is great than the RNG
					if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

					// At this point, the tool and the target block match
					parent.logInfo("ENTITYDEATH("+victimTypeName+"): Check successful (attempting to drop "+drop.dropped+")", 3);


					if (drop.exclusive != null) {
						if (exclusive == null) { 
							exclusive = drop.exclusive;
							toBeDropped.clear();
						}
					}

					if (exclusive != null)
					{
						if (drop.exclusive.equals(exclusive))
						{
							doDrop = true;
						} else {
							doDrop = false;
						}
					} else {
						doDrop = true;
					}

					if (!doDrop) continue;

					if(drop.dropped.equalsIgnoreCase("DEFAULT")) {
						doDefaultDrop = true;
					}

					if(drop.dropped.equalsIgnoreCase("DENY")) { 
						denyBreak = true;
					} else {
						if(event.getEntity() instanceof Player) {
							System.out.println("nodrop: "+drop.dropped);
							doDefaultDrop = true;
							if (drop.dropped.equalsIgnoreCase("NODROP")) {
								playerNoDrop = true;
							} else {
								toBeDropped.add(drop);
							}
						} else {	
							if (drop.dropped.equalsIgnoreCase("DEFAULT")) {
								doDefaultDrop = true;
							} else {
								toBeDropped.add(drop);
							}
						}
					}



					Integer currentAttackerDamage = drop.getRandomAttackerDamage();
					maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;
				}

				// Now do the drops
				if(toBeDropped.size() > 0 && doDefaultDrop == false) event.getDrops().clear();
				if(playerNoDrop) event.getDrops().clear();

				for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(location, obc, player);

				if(toBeDropped.size() > 0) {
					if (player != null) {
						player.damage(maxAttackerDamage);
					}
				}
			}
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
			boolean doDefaultDrop = false;

			List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();

			// TODO: remove me
			// test for version 2
			// grab the relevant collection of dropgroups
			parent.logInfo("DEBUG: Painting break: entityid="+event.getPainting().getEntityId(), 4);
			OBContainer_DropGroups dropGroups = parent.config.blocksHash.get("321");

			Short dataVal = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;

			// loop through dropgroups
			if (dropGroups == null) {
				parent.logWarning("PAINTINGBREAK: warning - dropGroups is null!", 3);
				return;
			}
			for (OBContainer_Drops dropGroup : dropGroups.list) {
				if(!dropGroup.compareTo(
								"PAINTING", 
								dataVal,
								weapon,
								victim.getWorld(),
								player,
								parent.permissionHandler)) {

							continue;
						}
					for (OB_Drop drop : dropGroup.list) {

						if(!drop.compareTo(
								"PAINTING", 
								dataVal,
								weapon,
								victim.getWorld(),
								player,
								parent.permissionHandler)) {

							continue;
						}

						// Check probability is great than the RNG
						if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

						if(drop.dropped.equalsIgnoreCase("DEFAULT")) {
							doDefaultDrop = true;
						} else {
							toBeDropped.add(drop);
						}
					}
				}
			

			// Now do the drops
			for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(location, obc, player);
			
			if (!doDefaultDrop) {
				event.getPainting().remove();
				event.setCancelled(true);
			}
		}
	}
}

