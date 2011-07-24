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

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.nijiko.permissions.PermissionHandler;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class OB_Drop extends AbstractDrop
{
	public String original;
	public String dropped;
	public List<String> tool;
	public List<String> toolExceptions;
	public List<String> worlds;
	public Integer damage;
	public Double chance;
	public List<String> messages;
	public String time;
    public List<String> regions;
	public List<String> weather;
	public List<String> biome;
	public List<String> event;
	public String height;
    public List<String> permissionGroups; // obseleted - use permissions
    public List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
    public List<String> permissions;
    public List<String> permissionsExcept;
    public String exclusive;
    public String attackRange;
    
	private Short originalDataMin;
    private Short originalDataMax;
    private Short dropDataMin;
    private Short dropDataMax;
	private Float quantityMin;
    private Float quantityMax;
    private Integer attackerDamageMin;
    private Integer attackerDamageMax;    
	
	private static Random rng = new Random();

	// Quantity getters and setters


	public Integer getRandomQuantityInt() {
                Double random = getRandomQuantityDouble();

                Integer intPart = random.intValue();
                // .intValue() discards the decimal place - round up if neccessary
                if (random - Double.valueOf(intPart.toString()) >= 0.5) {
                        intPart = intPart + 1;
                }
                return intPart;
	}

	public Double getRandomQuantityDouble() {
		//TODO: fix this function so we don't need to multiply by 100
		// this will cause an error if the number is almost max float
		// but a drop that high would crash the server anyway
		Float min = (quantityMin * 100);
		Float max = (quantityMax * 100);
		Integer val = min.intValue() + rng.nextInt(max.intValue() - min.intValue() + 1);
		Double doubleVal = Double.valueOf(val); 
		Double deciVal = doubleVal/100;
		return deciVal;
	}
	
	public String getQuantityRange() {
	    return (quantityMin.equals(quantityMax) ? quantityMin.toString() : quantityMin.toString() + "-" + quantityMax.toString());
	}
	
	public void setQuantity(Float val) {
	    try {
 	        this.setQuantity(val, val);
	    } catch(NullPointerException x) {
	        this.quantityMin = this.quantityMax = Float.valueOf(1);
	    }
	}
	
	public void setQuantity(Float low, Float high) {
	    if(low < high) {
	        this.quantityMin = low;
	        this.quantityMax = high;
	    } else {
	        this.quantityMax = low;
	        this.quantityMin = high;
	    }
	}
	
	// Data getters and setters
	public String getData() {
		if (this.originalDataMin == null) {
			return ("");
		} else if(this.originalDataMin == this.originalDataMax) {
			return ("@"+this.originalDataMin);
		} else {
			return ("@RANGE-"+this.originalDataMin+"-"+this.originalDataMax);
		}
	}
	
	public void setData(Short val) {
	    try {
	        this.setData(val, val);
	    } catch(NullPointerException x) {
	        this.originalDataMin = this.originalDataMax = null;
	    }
	}
	
	public void setData(Short low, Short high) {
	    if(low < high) {
	        this.originalDataMin = low;
	        this.originalDataMax = high;
	    } else {
	        this.originalDataMin = high;
	        this.originalDataMax = low;
	    }
	}
	
	public boolean isDataValid(Short test) {
	    if(this.originalDataMin == null || test == null) return true;
	    return (test >= this.originalDataMin && test <= this.originalDataMax);
	}

	// DROPData
	public String getDropDataRange() {
	    if (dropDataMin == null) return "";
	    return (dropDataMin.equals(dropDataMax) ? dropDataMin.toString() : dropDataMin.toString() + "-" + dropDataMax.toString());
	}

	public Short getRandomDropData()
	{
		if (dropDataMin == null) return Short.valueOf("0");
		if (dropDataMin == dropDataMax) return dropDataMin;
		
		Integer randomVal = (dropDataMin + rng.nextInt(dropDataMax - dropDataMin + 1));
		Short shortVal = Short.valueOf(randomVal.toString());
		return shortVal;
	}

	public void setDropData(Short val) {
	    try {
	        this.setDropData(val, val);
	    } catch(NullPointerException x) {
	        this.dropDataMin = this.dropDataMax = null;
	    }
	}
	
	public void setDropData(Short low, Short high) {
	    if(low < high) {
	        this.dropDataMin = low;
	        this.dropDataMax = high;
	    } else {
	        this.dropDataMin = high;
	        this.dropDataMax = low;
	    }
	}
	
	public boolean isDropDataValid(Short test) {
	    if(this.dropDataMin == null) return true;
	    return (test >= this.dropDataMin && test <= this.dropDataMax);
	}

	// Attacker Damage
	public Integer getRandomAttackerDamage()
	{
		if (attackerDamageMin == attackerDamageMax) return attackerDamageMin;
		
		Integer randomVal = (attackerDamageMin + rng.nextInt(attackerDamageMax - attackerDamageMin + 1));
		return randomVal;
	}

	public void setAttackerDamage(Integer val) {
	    try {
	        this.setAttackerDamage(val, val);
	    } catch(NullPointerException x) {
	        this.attackerDamageMin = this.attackerDamageMax = null;
	    }
	}
	
	public void setAttackerDamage(Integer low, Integer high) {
	    if(low < high) {
	        this.attackerDamageMin = low;
	        this.attackerDamageMax = high;
	    } else {
	        this.attackerDamageMin = high;
	        this.attackerDamageMax = low;
	    }
	}
	
	public boolean isAttackerDamageValid(Short test) {
	    if(this.attackerDamageMin == null) return true;
	    return (test >= this.attackerDamageMin && test <= this.attackerDamageMax);
	}
	
	// Comparison tests
	public boolean compareTo(Object eventObject, Short eventData, String eventTool, World eventWorld, Player player, OtherBlocks parent) {
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
			parent.logWarning("Starting drop compareto, string.",4);
			eventTarget = (String) eventObject;
		} else if (eventObject instanceof Block) {
			parent.logWarning("Starting drop compareto, block.",4);
			eventBlock = (Block) eventObject;
			eventHeight = eventBlock.getY();
			eventBiome = eventBlock.getBiome().name();
			eventTarget = eventBlock.getType().toString();
			eventInt = eventBlock.getTypeId();
			eventLocation = eventBlock.getLocation();
		} else if (eventObject instanceof Player) {
			parent.logWarning("Starting drop compareto, player.",4);
			eventPlayer = (Player) eventObject;
			eventTarget = eventPlayer.getName();
			eventHeight = eventPlayer.getLocation().getBlockY();
			eventBiome = eventPlayer.getLocation().getBlock().getBiome().name();
			eventLocation = eventPlayer.getLocation();
		} else if (eventObject instanceof Entity) {
			parent.logWarning("Starting drop compareto, entity.",4);
			eventEntity = (Entity) eventObject;

			eventTarget = "CREATURE_"+CommonEntity.getCreatureType(eventEntity).toString();
			eventInt = eventEntity.getEntityId();
			eventHeight = eventEntity.getLocation().getBlock().getY();
			eventBiome = eventEntity.getLocation().getBlock().getBiome().name();
			eventLocation = eventEntity.getLocation();
		} else {
			parent.logWarning("Starting drop compareto, unknown eventObject type.",4);
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
				parent.logWarning("Leaf decay - fixing data.",4);
				eventData = (short) ((0x3) & eventData);
			}
		}       


		// Check parameters
		try {
			OB_Drop drop = this;
			if (drop instanceof OB_Drop) {
				OB_Drop obDrop = (OB_Drop) drop; 

				// Check original data type if not null
				if(!obDrop.isDataValid(eventData)) return false;
				parent.logWarning("Passed data check.",4);

				if (!(parent.config.isLeafDecay(obDrop.original))) {
					checkTools(eventTool, obDrop.tool);
					checkToolsExcept(eventTool, obDrop.toolExceptions);
				}
			}

			parent.logWarning("Passed tool checks.",4);
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
		} catch(Throwable ex) {
			parent.logInfo(ex.getMessage(),4);
			return false;
		}

		// All tests passed - return true.
		parent.logWarning("Passed ALL checks.",4);
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
		if (loc == null || dropRegions.contains(null)) return;
		OtherBlocks.logInfo("Checking location: "+loc.toString()+" is in region: "+dropRegions.toString(), 5);
		Vector vec = new Vector(loc.getX(), loc.getY(), loc.getZ());
		OtherBlocks.logInfo("loc:"+loc.getX()+loc.getY()+loc.getZ()+" vec: "+vec.getX()+vec.getY()+vec.getZ());
		Map<String, ProtectedRegion>regions = OtherBlocks.worldguardPlugin.getGlobalRegionManager().get(loc.getWorld()).getRegions();
		OtherBlocks.logInfo(regions.keySet().toString());
		
		for (String key : regions.keySet()) {
			ProtectedRegion region = regions.get(key);
			if (region.contains(vec)) {
				OtherBlocks.logInfo("IN region: "+region.getId(), 5);
				return;
			}
		}
		throw new Exception("Failed check: regions");
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
                    OtherBlocks.logInfo("Inside tool check: looptool="+loopTool+" eventtoolorg="+eventToolOrg, 5);
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
		try {
			checkBasicList(eventWorld, dropWorlds);
		} catch(Exception ex) {
			throw ex;
		}
		OtherBlocks.logWarning("Passed worlds check.",4);
	}
	static void checkBiomes(String eventBiome, List<String> dropBiomes) throws Exception {
		try {
			checkBasicList(eventBiome, dropBiomes);
		} catch(Exception ex) {
			throw ex;
		}
		OtherBlocks.logWarning("Passed biome check.",4);
	}

	static void checkBasicList(String eventBiome, List<String> dropBiomes) throws Exception
	{
		Boolean biomeMatchFound = false;

		for(String loopBiome : dropBiomes) {
			if(loopBiome == null) {
				biomeMatchFound = true;
				break;
			} else {
				if(loopBiome.equalsIgnoreCase(eventBiome)) {
					biomeMatchFound = true;
					break;
				}
			}
		}
		if(!biomeMatchFound) throw new Exception("Failed biome check.");
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
		if(!weatherMatchFound) throw new Exception("Failed time check.");
		OtherBlocks.logWarning("Passed weather check.",4);
	}


	static void checkPermissions(Player player, List<String> permissions, PermissionHandler permissionHandler) throws Exception
	{
		if (permissionHandler == null || permissions.contains(null)) return;
		if (!checkPermissionsBase(player, permissions, permissionHandler)) throw new Exception();
		OtherBlocks.logWarning("Passed permissions checks.",4);
	}

	static void checkPermissionsExcept(Player player, List<String> permissionsExcept, PermissionHandler permissionHandler) throws Exception
	{
		// Permissions check
		if (permissionHandler == null || permissionsExcept.contains(null)) return;
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
		if (permissionHandler == null || permissionGroups.contains(null)) return;
		if (!checkPermissionGroupsBase(player, permissionGroups, permissionHandler, false))
			throw new Exception("Failed check: permission groups");

	
		OtherBlocks.logWarning("Passed permission group checks.",4);
}

	static void checkPermissionGroupsExcept(Player player, List<String> permissionGroups, PermissionHandler permissionHandler) throws Exception
	{
		if (permissionHandler == null || permissionGroups.contains(null)) return;
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


	private static boolean isDay(long currenttime){
		return ((currenttime % 24000) < 12000 && currenttime > 0 )|| (currenttime < 0 && (currenttime % 24000) < -12000);
	}

}
