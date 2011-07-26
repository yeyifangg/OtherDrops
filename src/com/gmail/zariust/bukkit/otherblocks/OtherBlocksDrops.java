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



import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AbstractDocument.LeafElement;

import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.plugin.Plugin;

import com.gmail.zariust.bukkit.common.CommonEntity;


public class OtherBlocksDrops  {

	public static void checkDrops(Event event, OtherBlocks parent) {

		Cancellable cancellableEvent = null;
		if (event instanceof Cancellable) {
			cancellableEvent = (Cancellable) event;
			if (cancellableEvent.isCancelled()) return;
		}

		Object eventObject = null; // this is the block or entity to pass to compareTo
		Short eventData = Short.valueOf("0"); // this is the data attached to current event object
		ItemStack tool = null;
		String toolString = null;
		Player player = null;
		Location location = null;
		World world = null;

		String victimName = "Undefined event";
		String victimTypeName = "";

		String eventType = event.getType().name();


		Block target = null;
		String eventTarget = null;

		BlockBreakEvent bbEvent = null;
		EntityDeathEvent edEvent = null;
		Entity edVictim = null;
		CreatureType edVictimType = null;
		
		
		VehicleDestroyEvent vdEvent = null;
		PaintingBreakEvent pbEvent = null;

		// Note: I've tried to cut out everything that I can occuring before the hashmap check
		// so that we can check as early as possible and exit in case of no match
		// The code in this following section is needed for the hashmap check and the permissions check
		
		// =============
		// == Blocks
		// =============
		if (event instanceof BlockBreakEvent) {
			bbEvent = (BlockBreakEvent) event;
			Integer blockInt = bbEvent.getBlock().getTypeId();
			eventTarget = blockInt.toString();
			player = bbEvent.getPlayer();
		// =============
		// == Creatures
		// =============
		}	else if (event instanceof EntityDeathEvent) {
			edEvent = (EntityDeathEvent) event;
			String weapon = parent.damagerList.get(edEvent.getEntity());
			edVictim = edEvent.getEntity();
			edVictimType = CommonEntity.getCreatureType(edVictim);

			if (edEvent.getEntity() instanceof Player) {
				Player victimPlayer = (Player)edEvent.getEntity();
				victimTypeName = victimPlayer.getName();
				eventTarget = "PLAYER";
			} else if(edVictimType == null) {
				return;
			} else {
				victimTypeName = edVictimType.toString();
				eventTarget = "CREATURE_"+CommonEntity.getCreatureType(edEvent.getEntity()).toString();
			}

			parent.logInfo("ENTITYDEATH("+eventTarget+" with "+weapon+"): before check.", 3);

			if (weapon.contains("@")) {
				String[] weaponSplit = weapon.split("@");
				if (weaponSplit[1].equalsIgnoreCase("SKELETON") || weaponSplit[1].equalsIgnoreCase("DISPENSER")) {
					// do nothing
					parent.logInfo("Skeleton or dispenser attack",3);
				} else {
					player = getPlayerFromWeapon(weapon, edVictim.getWorld());
					//weapon = weaponSplit[0];
				}
			}

			toolString = weapon;
		// =============
		// == Paintings
		// =============
		} else if (event instanceof PaintingBreakEvent) {
			pbEvent = (PaintingBreakEvent) event;
			if(event instanceof PaintingBreakByEntityEvent) {
				PaintingBreakByEntityEvent e = (PaintingBreakByEntityEvent) event;
				if(e.getRemover() instanceof Player) {
					Player damager = (Player) e.getRemover();
					parent.damagerList.put(e.getPainting(), damager.getItemInHand().getType().toString()+"@"+damager.getName());
				} else {
					CreatureType attacker = CommonEntity.getCreatureType(e.getRemover());
					if(attacker != null) {
						parent.damagerList.put(e.getPainting(), "CREATURE_" + attacker.toString());
					}
				}
			} else {

				// Damager was not a person - switch through damage types
				switch(pbEvent.getCause()) {
					case WORLD:
						parent.damagerList.put(pbEvent.getPainting(), "DAMAGE_WORLD");
						break;
					default:
						parent.damagerList.remove(pbEvent.getPainting());
						break;
				}
			}
			String weapon = parent.damagerList.get(pbEvent.getPainting());
			Entity victim = pbEvent.getPainting();

			player = getPlayerFromWeapon(weapon, victim.getWorld());					
			if (weapon.contains("@"))
				weapon = weapon.split("@")[0];

			eventObject = pbEvent.getPainting();
			location = victim.getLocation();
			world = victim.getWorld();
			toolString = weapon;
			eventTarget = "321"; //blockid of paintings
		// =============
		// == Leaf decay
		// =============
		} else if (event instanceof LeavesDecayEvent) {
			eventTarget = "SPECIAL_LEAFDECAY";
			player = null;
			
		// =============
		// == Vehicles
		// =============
		} else if (event instanceof VehicleDestroyEvent) {
			vdEvent = (VehicleDestroyEvent) event;
			Entity victim = vdEvent.getVehicle();
			Material victimType = CommonEntity.getVehicleType(victim);

			if(victimType == null) return;

			Integer victimIdInt = Material.getMaterial(victimType.toString()).getId();
			eventTarget = victimIdInt.toString(); // Note: hash is by string, so toString() is important
			eventObject = victimType.toString();
		}

		// Now that we have the eventTarget check if any drops exist, exit if not.
		//TODO: properly support creatures by integer value (for new itemcraft creatures)
		List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();
		parent.logInfo(eventType+"("+victimName+"): before check.", 3);
		// grab the relevant collection of dropgroups
		OBContainer_DropGroups dropGroups = parent.config.blocksHash.get(eventTarget);

		if (dropGroups == null) {
			parent.logWarning(eventType+"("+victimName+"): warning - dropGroups is null!", 3);
			return;
		}

		
		// =============
		// == Check permissions
		// =============
		boolean otherblocksActive = true;

		if (parent.permissionsPlugin != null && player != null) {
			parent.logInfo("BLOCKBREAK - starting check - permissions enabled. Checking '"+player.getName()+"' has 'otherblocks.active'.",4);
			if (!(parent.permissionHandler.has(player, "otherblocks.active"))) {
				parent.logInfo("BLOCKBREAK - starting check - permissions enabled. Checking '"+player.getName()+"' has NOT got 'otherblocks.active'.",4);
				otherblocksActive = false;
			}			
		} else {
			parent.logInfo("BLOCKBREAK - starting check - permissions disabled.",4);
		}

		if (otherblocksActive) {

			// ***************
			// ** Creatures
			// ***************
			if (event instanceof EntityDeathEvent) {
				world = edVictim.getWorld();
				eventObject = edVictim;
				location = edVictim.getLocation();

				if (edVictimType != null) eventData = getCreatureDataValue(edVictim, edVictimType.toString());
				parent.logInfo("OnEntityDeath: "+edEvent.getEntity().toString()+"@"+eventData+", by "+toolString+"@"+player+" in "+edVictim.getWorld().getName()+")", 3);
			// ***************
			// ** Blocks
			// ***************
			} else if (event instanceof BlockBreakEvent) {
				world = bbEvent.getBlock().getWorld();
				tool = player.getItemInHand();
				toolString = tool.getType().toString();
				eventData = (short) bbEvent.getBlock().getData();
				target = bbEvent.getBlock();
				eventObject = bbEvent.getBlock();
				location = bbEvent.getBlock().getLocation();
				if (tool.getData() != null) {
					toolString = toolString + "@"+String.valueOf(tool.getData().getData());
				}
			// ***************
			// ** Paintings
			// ***************
			} else if (event instanceof PaintingBreakEvent) {
				// grab the relevant collection of dropgroups
				parent.logInfo("DEBUG: Painting break: entityid="+pbEvent.getPainting().getEntityId(), 4);
				Entity victim = pbEvent.getPainting();
				eventData = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;
				String paintingString = "PAINTING";
				eventObject = paintingString;
				world = pbEvent.getPainting().getWorld();
			// ***************
			// ** Vehicles
			// ***************
			} else if (event instanceof VehicleDestroyEvent) {
				String weapon;
				if(vdEvent.getAttacker() instanceof Player) {
					player = (Player) vdEvent.getAttacker(); 
					weapon = player.getItemInHand().getType().toString();
				} else {
					CreatureType creatureType = CommonEntity.getCreatureType(vdEvent.getAttacker());
					if(creatureType == null) return;
					weapon = "CREATURE_" + creatureType.toString();
				}

				Entity victim = vdEvent.getVehicle();
				location = victim.getLocation();
				world = location.getWorld();
				eventData = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;
				toolString = weapon;
			} else if (event instanceof LeavesDecayEvent) {
				LeavesDecayEvent ldEvent = (LeavesDecayEvent) event;
				target = ldEvent.getBlock();
				// Get the leaf's data value
				// Beware of the 0x4 bit being set - use a bitmask of 0x3
				eventData = (short) ((0x3) & ldEvent.getBlock().getData());
				world = target.getWorld();
				toolString = Material.AIR.toString();
				eventObject = eventTarget;
				location = target.getLocation();
			}

			if (location == null || eventObject == null || toolString == null) {
				parent.logWarning("location||eventobject||toolstring is null... this shouldn't happen, please report this bug.");
				return;
			}

			Integer maxDamage = 0; // for tool damage on block breaks
			boolean doDefaultDrop = false;
			boolean denyBreak = false;
			boolean doDrop = true;
			String exclusive = null;
			String dropGroupExclusive = null;
			Integer maxAttackerDamage = 0;
			boolean playerNoDrop = false; // for entitydeaths

			// loop through dropgroups
			for (OBContainer_Drops dropGroup : dropGroups.list) {
				if(!dropGroup.compareTo(
						eventObject,
						eventData,
						toolString, 
						world,
						player,
						parent.permissionHandler)) {

					continue;
				}
				if (dropGroup.chance != null) {
					if(parent.rng.nextDouble() > (dropGroup.chance.doubleValue()/100)) continue;
				}
				if (dropGroup.name != null) {
					parent.logInfo("Dropgroup success - name: "+dropGroup.name,3);
				}

				if (dropGroup.exclusive != null) {
					if (dropGroupExclusive == null) { 
						dropGroupExclusive = dropGroup.exclusive;
						toBeDropped.clear();
					}
				}

				if (dropGroupExclusive != null)
				{
					if (dropGroup.exclusive == null) continue;

					if (!dropGroup.exclusive.equals(dropGroupExclusive))
					{
						continue;
					}
				}

				// Loop through drops
				for (OB_Drop drop : dropGroup.list) {
					parent.logInfo("BLOCKBREAK: before comparetoz.", 4);
					if(!drop.compareTo(
							eventObject,
							eventData,
							toolString, 
							world,
							player,
							parent)) {

						continue;
					}
					parent.logInfo("BLOCKBREAK: after compareto.", 4);

					// Check probability is great than the RNG
					if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

					// At this point, the tool and the target block match
					//successfulComparison = true;
					//if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
					parent.logInfo("ENTITYDEATH("+victimTypeName+"): Check successful (attempting to drop "+drop.dropped+")", 3);

					if (drop.exclusive != null) {
						if (exclusive == null) { 
							exclusive = drop.exclusive;
							toBeDropped.clear();
						}
					}

					doDrop = false;
					if (exclusive != null)
					{
						if (drop.exclusive == null) continue;

						if (drop.exclusive.equals(exclusive))
						{
							doDrop = true;
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
						if(eventObject instanceof Player) {
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
								toBeDropped.add(drop);
							} else {
								toBeDropped.add(drop);
							}
						}
					}


					maxDamage = (maxDamage < drop.damage) ? drop.damage : maxDamage;

					Integer currentAttackerDamage = drop.getRandomAttackerDamage();
					maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;
				}
			}

			// Now do the drops
			if (edEvent != null) {
				if(toBeDropped.size() > 0 && doDefaultDrop == false) edEvent.getDrops().clear();
				if(playerNoDrop) edEvent.getDrops().clear();
			}

			if (event instanceof LeavesDecayEvent) {
				// Note: for leafdecay it's dangerous to combine drops and denybreak
				// as this can lead to a full set of drops each time the leaves are checked
				// which can be simulated by placing a log or leaf next to another and destroying it
				// Hence: we disable drops here if denybreak is true.
				if (!denyBreak) {
					for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(target.getLocation(), obc, null);
				} else {
					if (toBeDropped.size() > 1)
						parent.logWarning("LEAFDECAY: DENYBREAK combined with drops on leaf decay is dangerous - disabling drops.", 2);
				}
			} else {
				for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(location, obc, player);
			};

			// Deal player damage if set
			if (player != null) {
				player.damage(maxAttackerDamage);
			}

			if(toBeDropped.size() > 0) {
				if (event instanceof BlockBreakEvent) {
					// save block name for later
					String blockName = bbEvent.getBlock().getType().toString();
					// Check the tool can take wear and tear
					if(tool.getType().getMaxDurability() < 0 || tool.getType().isBlock()) return;
	
					// Now adjust the durability of the held tool
					parent.logInfo("BLOCKBREAK("+blockName+"): doing "+maxDamage+" damage to tool.", 3);
					tool.setDurability((short) (tool.getDurability() + maxDamage));
	
					// Manually check whether the tool has exceed its durability limit
					if(tool.getDurability() >= tool.getType().getMaxDurability()) {
						player.setItemInHand(null);
					}
				}
			
				if (doDefaultDrop == false) {
					if (event instanceof BlockBreakEvent) {						
						// give a chance for logblock (or BigBrother, if available) to log the block destruction
						OtherBlocks.queueBlockBreak(bbEvent.getPlayer().getName(), bbEvent.getBlock());
	
						// Convert the target block
						// save block name for later
						String blockName = bbEvent.getBlock().getType().toString();
						parent.logInfo("BLOCKBREAK("+blockName+"): cancelling event and removing block.", 3);
						cancellableEvent.setCancelled(true);
						if (!denyBreak) target.setType(Material.AIR);	
					} else if (event instanceof LeavesDecayEvent) {
						// Convert the target block
						cancellableEvent.setCancelled(true);
						if (!denyBreak) {
							target.setType(Material.AIR);
						} else {
							// set data to make sure leafs don't keep trying to decay
							target.setData(eventData.byteValue());
						}
					} else if (event instanceof VehicleDestroyEvent) {
						// remove default drop
						cancellableEvent.setCancelled(true);
						vdEvent.getVehicle().remove();
					} else if (event instanceof PaintingBreakEvent) {
						pbEvent.getPainting().remove();
						cancellableEvent.setCancelled(true);
					}
				}

			}
		}

	}


	private static Player getPlayerFromWeapon(String weapon, World world) {
		if (weapon == null || world == null)
			return null;

		String playerName = "";
		Player player = null;
		if (weapon.contains("@")) {
			String[] weaponList = weapon.split("@");
			weapon = weaponList[0];
			playerName = weaponList[1];
			//List<Player> players = world.getPlayers();

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
	
	static Short getCreatureDataValue(Entity victim, String victimTypeName) {
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
		
		return 0;
	}
}
