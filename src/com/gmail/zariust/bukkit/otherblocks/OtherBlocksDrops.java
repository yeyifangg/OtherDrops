// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
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
import java.util.Map;

import javax.swing.text.AbstractDocument.LeafElement;

import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
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
import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.nijiko.permissions.PermissionHandler;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class OtherBlocksDrops  {

	public static boolean canPlayerBuild(Player player, Block block) {
		// Check if player has build permissions - if not, exit
		if (OtherBlocks.permissionHandler != null) {
			String worldName = player.getWorld().getName();
			String[] groups = OtherBlocks.permissionHandler.getGroups(worldName, player.getName());
			boolean canBuild = false;
			for (String group : groups) {
				if (OtherBlocks.permissionHandler.canGroupBuild(worldName, group)) {
					canBuild = true;
				}
			}
			if (!canBuild) return false;
		}
		// Check if player has WorldGuard region build permissions - if not, exit
		if (OtherBlocks.worldguardPlugin != null) {
			if (!(OtherBlocks.worldguardPlugin.canBuild(player, block))) return false;
		}	
		return true;
	}
	
	
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

			if (!canPlayerBuild(player, bbEvent.getBlock())) return;
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
					if (!canPlayerBuild(damager, pbEvent.getPainting().getLocation().getBlock())) {
						pbEvent.setCancelled(true);
						return;
					}
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
			Entity attacker = vdEvent.getAttacker();
			if (attacker instanceof Player) {
				Player damager = (Player) attacker;
				if (!canPlayerBuild(damager, vdEvent.getVehicle().getLocation().getBlock())) {
					vdEvent.setCancelled(true);
					return;
				}
			}
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
		// == Check permissions  #### NOT NEEDED ANYMORE - see "canPlayerBuild" function
		// =============
		/* boolean otherblocksActive = true;

		if (parent.permissionsPlugin != null && player != null) {
			OtherBlocks.logInfo("BLOCKBREAK - starting check - permissions enabled. Checking '"+player.getName()+"' has 'otherblocks.active'.",4);
			if (!(parent.permissionHandler.has(player, "otherblocks.active"))) {
				OtherBlocks.logInfo("BLOCKBREAK - starting check - permissions enabled. Checking '"+player.getName()+"' has NOT got 'otherblocks.active'.",4);
				otherblocksActive = false;
			}			
		} else {
			parent.logInfo("BLOCKBREAK - starting check - permissions disabled.",4);
		}

		if (otherblocksActive) {*/

			// ***************
			// ** Creatures
			// ***************
			if (event instanceof EntityDeathEvent) {
				world = edVictim.getWorld();
				eventObject = edVictim;
				location = edVictim.getLocation();

				if (edVictimType != null) eventData = getCreatureDataValue(edVictim, edVictimType.toString());
				OtherBlocks.logInfo("OnEntityDeath: "+edEvent.getEntity().toString()+"@"+eventData+", by "+toolString+"@"+player+" in "+edVictim.getWorld().getName()+")", 3);
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
				OtherBlocks.logWarning("location||eventobject||toolstring is null... this shouldn't happen, please report this bug.");
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
			String replacementBlock = "";

			// loop through dropgroups
			for (OBContainer_Drops dropGroup : dropGroups.list) {
				if (dropGroup.tool != null) OtherBlocks.logInfo("BLOCKBREAK: before compareto (dropgroup)."+dropGroup.tool, 4);
				if(!OtherBlocksDrops.compareTo(
						dropGroup,
						eventObject,
						eventData,
						toolString, 
						world,
						player,
						parent)) {

					continue;
				}
				OtherBlocks.logInfo("BLOCKBREAK: after compareto (dropgroup).", 4);
				if (dropGroup.chance != null) {
					if(parent.rng.nextDouble() > (dropGroup.chance.doubleValue()/100)) continue;
				}
				if (dropGroup.name != null) {
					OtherBlocks.logInfo("Dropgroup success - name: "+dropGroup.name,3);
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

				// show message
				OtherBlocks.sendPlayerRandomMessage(player, dropGroup.messages, "");
					
				// Loop through drops
				for (OB_Drop drop : dropGroup.list) {
					OtherBlocks.logInfo("BLOCKBREAK: before compareto (drop: "+drop.dropped+").", 4);
					if(!OtherBlocksDrops.compareTo(
							drop,
							eventObject,
							eventData,
							toolString, 
							world,
							player,
							parent)) {

						continue;
					}
					OtherBlocks.logInfo("BLOCKBREAK: after compareto (drop).", 4);

					// Check probability is great than the RNG
					if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

					// At this point, the tool and the target block match
					//successfulComparison = true;
					//if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
					OtherBlocks.logInfo("ENTITYDEATH("+victimTypeName+"): Check successful (attempting to drop "+drop.dropped+")", 3);

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
							if (drop.replacementBlock != null) {
								int rnd = AbstractDrop.rng.nextInt(drop.replacementBlock.size());
								replacementBlock = drop.replacementBlock.get(rnd);
								OtherBlocks.logInfo("Setting replacementblock to "+replacementBlock, 4);
							}
							if (drop.dropped.equalsIgnoreCase("DEFAULT")) {
								doDefaultDrop = true;
								toBeDropped.add(drop);
							} else {
								toBeDropped.add(drop);
							}
						}
					}


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
				for(OB_Drop drop : toBeDropped) {
					maxDamage = (maxDamage < drop.damage) ? drop.damage : maxDamage;

					Integer currentAttackerDamage = drop.getRandomAttackerDamage();
					maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;

					OtherBlocks.performDrop(location, drop, player);
				}
			};

			// Deal player damage if set
			if (player != null) {
				player.damage(maxAttackerDamage);
			}

			if(toBeDropped.size() > 0 || denyBreak) {
				if (event instanceof BlockBreakEvent) {
					// save block name for later
					String blockName = bbEvent.getBlock().getType().toString();
					// Check the tool can take wear and tear
					if ( !(tool.getType().getMaxDurability() < 0 || tool.getType().isBlock())) {
		
						// Now adjust the durability of the held tool
						parent.logInfo("BLOCKBREAK("+blockName+"): doing "+maxDamage+" damage to tool.", 3);
						tool.setDurability((short) (tool.getDurability() + maxDamage));
		
						// Manually check whether the tool has exceed its durability limit
						if(tool.getDurability() >= tool.getType().getMaxDurability()) {
							player.setItemInHand(null);
						}
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
						if (!denyBreak) { 
							Material replacementMaterial = Material.AIR;
							try {
								replacementMaterial = Material.valueOf(replacementBlock);
							} catch (Exception ex) {}
							target.setType(replacementMaterial);	
						}
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
	//	}

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
		} else if(victimTypeName == "SLIME") {
			Slime slime = (Slime)victim;
			dataVal = (short)slime.getSize();
		}
		
		return dataVal;
	}


	// Comparison tests
	public static boolean compareTo(AbstractDrop drop, Object eventObject, Short eventData, String eventTool, World eventWorld, Player player, OtherBlocks parent) {
		PermissionHandler permissionHandler = parent.permissionHandler;
		String eventTarget = null;
		Integer eventInt = null;
		Entity eventEntity = null;
		Player eventPlayer = null;
		Block eventBlock = null;
		String victimPlayerName = null;
		String victimPlayerGroup = null;
		Integer eventHeight = null;
		String eventBiome = null;
		Location eventLocation = null;

		if (eventObject instanceof String) {
			OtherBlocks.logWarning("Starting drop compareto, string.",4);
			eventTarget = (String) eventObject;
		} else if (eventObject instanceof Block) {
			OtherBlocks.logWarning("Starting drop compareto, block.",4);
			eventBlock = (Block) eventObject;
			eventHeight = eventBlock.getY();
			eventBiome = eventBlock.getBiome().name();
			eventTarget = eventBlock.getType().toString();
			eventInt = eventBlock.getTypeId();
			eventLocation = eventBlock.getLocation();
		} else if (eventObject instanceof Player) {
			OtherBlocks.logWarning("Starting drop compareto, player.",4);
			eventPlayer = (Player) eventObject;
			eventTarget = eventPlayer.getName();
			eventHeight = eventPlayer.getLocation().getBlockY();
			eventBiome = eventPlayer.getLocation().getBlock().getBiome().name();
			eventLocation = eventPlayer.getLocation();
		} else if (eventObject instanceof Entity) {
			OtherBlocks.logWarning("Starting drop compareto, entity.",4);
			eventEntity = (Entity) eventObject;

			eventTarget = "CREATURE_"+CommonEntity.getCreatureType(eventEntity).toString();
			eventInt = eventEntity.getEntityId();
			eventHeight = eventEntity.getLocation().getBlock().getY();
			eventBiome = eventEntity.getLocation().getBlock().getBiome().name();
			eventLocation = eventEntity.getLocation();
		} else {
			OtherBlocks.logWarning("Starting drop compareto, unknown eventObject type.",4);
		}

		// Check original block - synonyms here
		// Don't need this - checked by hashMap
		/*                try {
                        Integer originalInt = Integer.valueOf(drop.original);
                        if (!originalInt.equals(eventInt)) return false;
                } catch(NumberFormatException x) {
                        if (drop.original.startsWith("PLAYER")) {
                                if(eventPlayer != null) {
                                        if (!drop.original.equalsIgnoreCase("PLAYER")) {
                                                if (!(drop.original.equalsIgnoreCase("PLAYER@"+eventPlayer.getName()))) {
                                                        return false;
                                                }
                                        }
                                } else {
                                        return false;
                                }
                    } else if (drop.original.startsWith("PLAYERGROUP@")) {
                                if(eventPlayer != null) {
                                String groupName = OtherBlocks.getDataEmbeddedDataString(drop.original);
                                if (groupName == null || groupName.isEmpty()) return false;

                                if (permissionHandler != null) {
                                        if (!(permissionHandler.inGroup(eventWorld.getName(), eventPlayer.getName(), groupName))) {
                                                return false;
                                        }
                                } else {
                                        return false;
                                }
                        } else {
                                return false;
                        }
                    } else if(CommonMaterial.isValidSynonym(drop.original)) {
                        if(!CommonMaterial.isSynonymFor(drop.original, Material.getMaterial(eventTarget))) return false;
                    } else if(CommonEntity.isValidSynonym(drop.original)) {
                        if(!CommonEntity.isSynonymFor(drop.original, CreatureType.fromName(eventTarget))) return false;
                    } else {
                        if(!drop.original.equalsIgnoreCase(eventTarget)) {
                                parent.logWarning("dropCompareTo: "+drop.original+" does not match "+eventTarget+", exiting.",4);
                                return false;
                        }
                    }
                }
                parent.logWarning("Passed block check.",4);*/

		// TODO: do we need this here or in the top of checkDrops
		// Cater for the fact that bit 4 of leaf data is set depending on decay check
		if (Material.getMaterial(eventTarget) != null) {
			if (Material.getMaterial(eventTarget).name() == "LEAVES") {
				// Beware of the 0x4 bit being set - use a bitmask of 0x3
				OtherBlocks.logWarning("Leaf decay - fixing data.",4);
				eventData = (short) ((0x3) & eventData);
			}
		}       


		// Check parameters
		try {
			//OB_Drop drop = this;
			if (drop instanceof OB_Drop) {
				OB_Drop obDrop = (OB_Drop) drop; 

				// Check original data type if not null
				if(!obDrop.isDataValid(eventData)) return false;
				OtherBlocks.logWarning("Passed data check.",4);
			}
			
			if (!(OtherBlocksConfig.isLeafDecay(drop.original))) {
				checkTools(eventTool, drop.tool);
				checkToolsExcept(eventTool, drop.toolExceptions);
			}
			

			OtherBlocks.logWarning("Passed tool checks. (tool = "+eventTool.toString()+")",4);
            String[] eventToolSplit = eventTool.split("@");

			checkWorlds(eventWorld.getName(), drop.worlds);
			checkTime(eventWorld, drop.time);        
			checkWeather(eventWorld, drop.weather);
			checkHeight(eventHeight, drop.height);
			checkAttackRange(eventToolSplit, drop.attackRange);
			checkBiomes(eventBiome, drop.biome);
			checkPermissions(player, drop.permissions, permissionHandler);
			checkPermissionsExcept(player, drop.permissionsExcept, permissionHandler);
			checkPermissionGroups(player, drop.permissionGroups, permissionHandler);
			checkPermissionGroupsExcept(player, drop.permissionGroupsExcept, permissionHandler);
			checkRegions(eventLocation, drop.regions);
			checkLightLevel(eventLocation, drop.lightLevel);
		} catch(Throwable ex) {
			OtherBlocks.logInfo(ex.getMessage(),4);
			//if (OtherBlocksConfig.verbosity > 2) ex.printStackTrace();
			return false;
		}

		// All tests passed - return true.
		OtherBlocks.logWarning("Passed ALL checks.",4);
		return true;
	}

	static void checkAttackRange(String[] eventToolSplit, String attackRange) throws Exception {
        // range check
        if (eventToolSplit.length > 2) {
                Integer eventrange = Integer.valueOf(eventToolSplit[2]);
                OtherBlocks.logInfo("In range check: eventRange = "+eventrange+" attackrange = "+attackRange,5);
                Boolean rangeMatchFound = false;
                if (attackRange != null) {
                        //System.out.println(eventrange+attackRange.substring(0,1)+attackRange.substring(1));
                        if (attackRange.substring(0, 1).equalsIgnoreCase("<")) {
                                if (eventrange < Integer.valueOf(attackRange.substring(1))) {
                                        rangeMatchFound = true;
                                }
                        } else if (attackRange.substring(0, 1).equalsIgnoreCase("=")) {
                                if (eventrange == Integer.valueOf(attackRange.substring(1))) {
                                        rangeMatchFound = true;
                                }
                        } else if (attackRange.substring(0, 1).equalsIgnoreCase(">")) {
                                if (eventrange > Integer.valueOf(attackRange.substring(1))) {
                                        rangeMatchFound = true;
                                }
                        }   
                } else {
                        rangeMatchFound = true;
                }
                if(!rangeMatchFound) throw new Exception("Failed check: attackrange");
                OtherBlocks.logWarning("Passed range check.",4);
        } else {
        	OtherBlocks.logWarning("Skipped range check.",4);
        }
		OtherBlocks.logWarning("Passed attackrange check.",4);
	}
	
	static void checkRegions(Location loc, List<String> dropRegions) throws Exception {
		if (null == dropRegions) return;
		if (loc == null || dropRegions.contains(null) || dropRegions.isEmpty()) return;

		OtherBlocks.logInfo("Checking location: "+loc.toString()+" is in region: "+dropRegions.toString(), 4);
		Vector vec = new Vector(loc.getX(), loc.getY(), loc.getZ());
		Map<String, ProtectedRegion>regions = OtherBlocks.worldguardPlugin.getGlobalRegionManager().get(loc.getWorld()).getRegions();
		
		List<String> inRegions = new ArrayList<String>();
		Boolean matchedRegion = true;
		
		for (String key : regions.keySet()) {			
			ProtectedRegion region = regions.get(key);
			if (region.contains(vec)) {
				inRegions.add(key);
			}
		}
		
		for (String dropRegion : dropRegions) {
			Boolean exception = false;
			if (dropRegion.startsWith("-")) {
				OtherBlocks.logInfo("Region exception: " + dropRegion, 4);
				exception = true;
				dropRegion = dropRegion.substring(1);
			} else {
				OtherBlocks.logInfo("Region: " + dropRegion, 4);
			}

			if (exception) {
				if (inRegions.contains(dropRegion)) {
					throw new Exception("Failed check: regions");					
				} 
			} else {
				if (inRegions.contains(dropRegion)) {
					OtherBlocks.logInfo("IN region: "+dropRegion, 4);
					return;
				} else {
					matchedRegion = false;					
				}

			}
		}

		if (matchedRegion) {
			OtherBlocks.logInfo("Passed region check...", 4);
			return;
		} else {
			throw new Exception("Failed check: regions");
		}
	}
	
	static void checkTools(String eventTool, List<String> dropTools) throws Exception {
		if (!checkToolsBase(eventTool, dropTools))
			throw new Exception("Failed check: tools");

	}

	static void checkToolsExcept(String eventTool, List<String> dropTools) throws Exception {
		if (dropTools == null) return; // toolexcept is option - no issue if nothing is set
		if (checkToolsBase(eventTool, dropTools))
			throw new Exception("Failed check: tools except");

	}

	static boolean checkToolsBase(String eventTool, List<String> dropTools) {
        // Check test case tool exists in array - synonyms here
        Boolean toolMatchFound = false;

        String eventToolOrg = eventTool;
        String[] eventToolSplit = eventTool.split("@");
    eventTool = eventToolSplit[0];
    String eventToolData = "0";
    if (eventToolSplit.length > 1) eventToolData = eventToolSplit[1];
        
        for(String loopTool : dropTools) {                
                    if(loopTool == null) {
                        toolMatchFound = true;
                        break;
                    } else if(CommonMaterial.isValidSynonym(loopTool)) {
                        if(CommonMaterial.isSynonymFor(loopTool, Material.getMaterial(eventTool))) {
                            toolMatchFound = true;
                            break;
                            // TODO: why is CommonEntity here?
                        //} else if(CommonEntity.isValidSynonym(this.original)) {
                            //toolMatchFound = true;
                            //break;
                        }
                    } else {
                        // check for specific damage type (eg. skeleton attack)
                            String[] loopSplit = loopTool.split("@");
                            if (loopSplit.length > 1) {
                            if(loopTool.equalsIgnoreCase(eventToolOrg)) {
                                    toolMatchFound = true;
                                    break;
                            } else {
                                    try {
                                            Short result = CommonMaterial.getAnyDataShort(eventTool, loopSplit[1]);
                                            if (result != null) {
                                                    String loopString = loopTool+"@"+String.valueOf(result);
                                                    if(eventToolData.equalsIgnoreCase(result.toString())) {
                                                            if(eventTool.equalsIgnoreCase(loopSplit[0])) {
                                                            toolMatchFound = true;
                                                            break;
                                                            }
                                                    }
                                            }
                                    } catch (Exception ex) {}
                            }
                        } else if (loopTool.equalsIgnoreCase(eventTool)) {
                            toolMatchFound = true;
                        } else if (eventTool.equalsIgnoreCase("DAMAGE_ENTITY_ATTACK")) { // allow for wildcard
                                    if (loopTool.equalsIgnoreCase(eventToolData)) {
                                            toolMatchFound = true;
                                            break;
                                    }
                            }

                    }
        }
        
		if(!toolMatchFound)
			return false;
		else
			return true;
	}     

	static void checkHeight(Integer eventHeight, String height) throws Exception
	{
		Boolean heightMatchFound = false;
		if (height != null) {
			//           parent.logInfo("checking height - "+eventHeight+height.substring(0,1)+height.substring(1),4);
			if (height.substring(0, 1).equalsIgnoreCase("<")) {
				if (eventHeight < Integer.valueOf(height.substring(1))) {
					heightMatchFound = true;
				}
			} else if (height.substring(0, 1).equalsIgnoreCase("=")) {
				if (eventHeight == Integer.valueOf(height.substring(1))) {
					heightMatchFound = true;
				}
			} else if (height.substring(0, 1).equalsIgnoreCase(">")) {
				if (eventHeight > Integer.valueOf(height.substring(1))) {
					heightMatchFound = true;
				}
			}   
		} else {
			heightMatchFound = true;
		}
		if(!heightMatchFound) throw new Exception("Failed height check.");
		OtherBlocks.logWarning("Passed height check.",4);
	}

	static void checkWorlds(String eventWorld, List<String> dropWorlds) throws Exception {
		if (!checkBasicList(eventWorld, dropWorlds)) throw new Exception("Failed worlds check.");
		OtherBlocks.logWarning("Passed worlds check.",4);
	}
	static void checkBiomes(String eventBiome, List<String> dropBiomes) throws Exception {
		if (!checkBasicList(eventBiome, dropBiomes)) throw new Exception("Failed biome check.");
		OtherBlocks.logWarning("Passed biome check.",4);
	}

	static boolean checkBasicList(String eventBiome, List<String> dropBiomes)
	{
		Boolean biomeMatchFound = false;
		if (dropBiomes == null || dropBiomes.isEmpty()) return true;

		OtherBlocks.logInfo("Checking: "+eventBiome+" is in list: "+dropBiomes.toString(), 4);
		
		// quick check
		if (dropBiomes.contains(eventBiome)) return true;
		if (dropBiomes.contains("-"+eventBiome)) return false;
		
		// more detailed check
		Boolean foundException = false; 
		for(String loopBiome : dropBiomes) {
			if(loopBiome == null) {
				biomeMatchFound = true;
				break;
			} else {
				Boolean exception = false;
				if (loopBiome.startsWith("-")) {
					exception = true;
					foundException = true;
					loopBiome = loopBiome.substring(1);
				}
				if (exception) {
					if(loopBiome.equalsIgnoreCase(eventBiome)) {
						return false;
					}
				} else {
					if(loopBiome.equalsIgnoreCase(eventBiome)) {
						biomeMatchFound = true;
						break;
					}
				}
			}
		}
		if (foundException) {
			// if we found an exception and got here then none of the exceptions matched
			return true;
		} else {
			if(!biomeMatchFound) return false;
			return true;
		}
	}

	static void checkTime(World eventWorld, String dropTime) throws Exception
	{
		if (dropTime != null && dropTime != "null") {
			String currentTime;
			if(isDay(eventWorld.getTime())) {
				currentTime = "DAY";
			} else {
				currentTime = "NIGHT";
			}
			OtherBlocks.logWarning("Timecheck: currentTime -"+currentTime+" thisTime - "+dropTime,4);
			if (!currentTime.equalsIgnoreCase(dropTime)) throw new Exception("Failed time check.");
		}
		OtherBlocks.logWarning("Passed time check.",4);
	}

	static void checkWeather(World eventWorld, List<String> dropWeather) throws Exception
	{
		Boolean weatherMatchFound = false;
		if (dropWeather == null || dropWeather.isEmpty()) return;
		
		for(String loopWorld : dropWeather) {
			if(loopWorld == null) {
				weatherMatchFound = true;
				break;
			} else {
				if (eventWorld.isThundering()) {
					if(loopWorld.equalsIgnoreCase("THUNDER") ||
							loopWorld.equalsIgnoreCase("THUNDERING") ||
							loopWorld.equalsIgnoreCase("LIGHTNING"))
					{
						weatherMatchFound = true;
						break;
					}
				} else if (eventWorld.hasStorm()) {
					if(loopWorld.equalsIgnoreCase("RAIN") ||
							loopWorld.equalsIgnoreCase("RAINY") ||
							loopWorld.equalsIgnoreCase("RAINING"))       
					{
						weatherMatchFound = true;
						break;
					}
				} else {
					if (loopWorld.equalsIgnoreCase("SUNNY") ||
							loopWorld.equalsIgnoreCase("CLEAR"))
					{
						weatherMatchFound = true;
						break;                          
					}
				}
			
			}
		}
		if(!weatherMatchFound) throw new Exception("Failed weather check.");
		OtherBlocks.logWarning("Passed weather check.",4);
	}


	static void checkPermissions(Player player, List<String> permissions, PermissionHandler permissionHandler) throws Exception
	{
		if (null == permissions) return;
		if (permissionHandler == null || permissions.contains(null) || permissions.isEmpty()) return;
		if (!checkPermissionsBase(player, permissions, permissionHandler)) throw new Exception();
		OtherBlocks.logWarning("Passed permissions checks.",4);
	}

	static void checkPermissionsExcept(Player player, List<String> permissionsExcept, PermissionHandler permissionHandler) throws Exception
	{
		if (null == permissionsExcept) return;
		// Permissions check
		if (permissionHandler == null || permissionsExcept.contains(null) || permissionsExcept.isEmpty()) return;
		if (checkPermissionsBase(player, permissionsExcept, permissionHandler)) throw new Exception();
		OtherBlocks.logWarning("Passed permissions except checks.",4);
	}

	static boolean checkPermissionsBase(Player player, List<String> dropPermissions, PermissionHandler permissionHandler)
	{
		
		Boolean permissionFound = false;

		for(String loopGroup : dropPermissions) {
			if(loopGroup == null) {
				permissionFound = true;
				break;
			} else {

				if (player != null) {
					if (permissionHandler != null) {
						if (permissionHandler.has(player, "otherblocks.custom."+loopGroup)) {
							permissionFound = true;
							break;
						}
					}
				}

			}
		}
		if(!permissionFound) return false;
		return true;
	}

	static void checkPermissionGroups(Player player, List<String> permissionGroups, PermissionHandler permissionHandler) throws Exception
	{
		if (null == permissionGroups) return;
		if (permissionHandler == null || permissionGroups.contains(null) || permissionGroups.isEmpty()) return;
		if (!checkPermissionGroupsBase(player, permissionGroups, permissionHandler, false))
			throw new Exception("Failed check: permission groups");

	
		OtherBlocks.logWarning("Passed permission group checks.",4);
}

	static void checkPermissionGroupsExcept(Player player, List<String> permissionGroups, PermissionHandler permissionHandler) throws Exception
	{
		if (null == permissionGroups) return;
		if (permissionHandler == null || permissionGroups.contains(null) || permissionGroups.isEmpty()) return;
		if (!checkPermissionGroupsBase(player, permissionGroups, permissionHandler, true))
			throw new Exception("Failed check: permission groups except");
		
		OtherBlocks.logWarning("Passed permissiongroups except checks.",4);
	}

	static boolean checkPermissionGroupsBase(Player player, List<String> permissionGroups, PermissionHandler permissionHandler, boolean except)
	{
		Boolean groupMatchFound = false;

		for(String loopGroup : permissionGroups) {
			if(loopGroup == null) {
				if (except)
					groupMatchFound = false;
				else 
					groupMatchFound = true;
				break;
			} else {

				if (player != null) {
					if (permissionHandler != null) {
						//if (permissionHandler.inGroup(eventWorld.getName(), player.getName(), loopGroup)) {
						// TODO: does this (player.getWorld rather than eventWorld) work properly? No unintended side-effects? 
						if (permissionHandler.inGroup(player.getWorld().getName(), player.getName(), loopGroup)) {
							groupMatchFound = true;
							break;
						}
					}
				}

			}
		}
		if (except) {
			if(groupMatchFound) return false;
		} else {
			if(!groupMatchFound) return false;
		}
		return true;
	}

	static void checkLightLevel(Location loc, String lightLevel) throws Exception {
		if (loc == null) return;
		if (lightLevel == null) return;
		
		Block block = loc.getBlock();
		
		Byte eventLightLevel = block.getLightLevel();
		if (eventLightLevel == 0) { // if not transparent, get maximum light level
			block.getRelative(BlockFace.UP).getLightLevel();
			Byte lightLevel2 = block.getRelative(BlockFace.EAST).getLightLevel(); if (lightLevel2 > eventLightLevel) eventLightLevel = lightLevel2;
			lightLevel2 = block.getRelative(BlockFace.WEST).getLightLevel(); if (lightLevel2 > eventLightLevel) eventLightLevel = lightLevel2;
			lightLevel2 = block.getRelative(BlockFace.NORTH).getLightLevel(); if (lightLevel2 > eventLightLevel) eventLightLevel = lightLevel2;
			lightLevel2 = block.getRelative(BlockFace.SOUTH).getLightLevel(); if (lightLevel2 > eventLightLevel) eventLightLevel = lightLevel2;
			lightLevel2 = block.getRelative(BlockFace.DOWN).getLightLevel(); if (lightLevel2 > eventLightLevel) eventLightLevel = lightLevel2;
		}
		
		OtherBlocks.logInfo("Checking that lightlevel - maximum: " +String.valueOf(eventLightLevel) + " is "+String.valueOf(lightLevel), 4);
	
		Boolean matchFound = false;
			//           parent.logInfo("checking height - "+eventHeight+height.substring(0,1)+height.substring(1),4);
			if (lightLevel.substring(0, 1).equalsIgnoreCase("<")) {
				if (eventLightLevel < Byte.valueOf(lightLevel.substring(1))) {
					matchFound = true;
				}
			} else if (lightLevel.substring(0, 1).equalsIgnoreCase("=")) {
				if (eventLightLevel == Byte.valueOf(lightLevel.substring(1))) {
					matchFound = true;
				}
			} else if (lightLevel.substring(0, 1).equalsIgnoreCase(">")) {
				if (eventLightLevel > Byte.valueOf(lightLevel.substring(1))) {
					matchFound = true;
				}
			}   
		if(!matchFound) throw new Exception("Failed check: lightlevel.");
		OtherBlocks.logWarning("Passed lightlevel check.",4);

	
	}

	private static boolean isDay(long currenttime){
		return ((currenttime % 24000) < 12000 && currenttime > 0 )|| (currenttime < 0 && (currenttime % 24000) < -12000);
	}


}
