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

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zarius.common.CommonEntity;
import com.gmail.zarius.common.CommonMaterial;
import com.nijiko.permissions.PermissionHandler;

public class OB_Drop
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
	public List<String> weather;
	public List<String> biome;
	public List<String> event;
	public String height;
    public List<String> permissionGroups; // obseleted - use permissions
    public List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
    public List<String> permissions;
    public List<String> permissionsExcept;
    public String exclusive;
    
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
	    if(this.originalDataMin == null) return true;
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
	// TODO: passing both eventtarget
	//public boolean compareTo(Block eventTargetBlock, Entity eventTargetEnt, Short eventData, String eventTool, World eventWorld) {
		//String eventTarget;
	public boolean compareTo(Object eventObject, Short eventData, String eventTool, World eventWorld, Player player, PermissionHandler permissionHandler) {
		
		String eventTarget = null;
		Integer eventInt = null;
		Entity eventEntity = null;
		Player eventPlayer = null;
		Block eventBlock = null;
		String victimPlayerName = null;
		String victimPlayerGroup = null;
		Integer eventHeight = null;
		String biomeName = null;
		
		if (eventObject instanceof String) {
			eventTarget = (String) eventObject;
		} else if (eventObject instanceof Block) {
			eventBlock = (Block) eventObject;
			eventHeight = eventBlock.getY();
			biomeName = eventBlock.getBiome().name();
			eventTarget = eventBlock.getType().toString();
			eventInt = eventBlock.getTypeId();
		} else if (eventObject instanceof Player) {
			eventPlayer = (Player) eventObject;
			eventTarget = eventPlayer.getName();
			eventHeight = eventPlayer.getLocation().getBlockY();
			biomeName = eventPlayer.getLocation().getBlock().getBiome().name();
		} else if (eventObject instanceof Entity) {
			eventEntity = (Entity) eventObject;
			
			eventTarget = "CREATURE_"+CommonEntity.getCreatureType(eventEntity).toString();
			eventInt = eventEntity.getEntityId();
			eventHeight = eventEntity.getLocation().getBlock().getY();
			biomeName = eventEntity.getLocation().getBlock().getBiome().name();
		}

		// Check original block - synonyms here
		try {
			Integer originalInt = Integer.valueOf(this.original);
			if (!originalInt.equals(eventInt)) return false;
		} catch(NumberFormatException x) {
			if (this.original.startsWith("PLAYER")) {
				if(eventPlayer != null) {
					if (!this.original.equalsIgnoreCase("PLAYER")) {
						if (!(this.original.equalsIgnoreCase("PLAYER@"+eventPlayer.getName()))) {
							return false;
						}
					}
				} else {
					return false;
				}
		    } else if (this.original.startsWith("PLAYERGROUP@")) {
				if(eventPlayer != null) {
		    		String groupName = OtherBlocks.getDataEmbeddedDataString(this.original);
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
		    } else if(CommonMaterial.isValidSynonym(this.original)) {
		    	if(!CommonMaterial.isSynonymFor(this.original, Material.getMaterial(eventTarget))) return false;
		    } else if(CommonEntity.isValidSynonym(this.original)) {
		    	if(!CommonEntity.isSynonymFor(this.original, CreatureType.fromName(eventTarget))) return false;
		    } else {
		    	if(!this.original.equalsIgnoreCase(eventTarget)) return false;
		    }
		}

	    // Cater for the fact that bit 4 of leaf data is set depending on decay check
	    if (Material.getMaterial(eventTarget) != null) {
	    	if (Material.getMaterial(eventTarget).name() == "LEAVES") {
	    		// Beware of the 0x4 bit being set - use a bitmask of 0x3
	    		eventData = (short) ((0x3) & eventData);
	    	}
	    }	    
	    
	    // Check original data type if not null
	    if(!this.isDataValid(eventData)) return false;
	    
	    // Check test case tool exists in array - synonyms here
	    Boolean toolMatchFound = false;
	    
	    for(String loopTool : this.tool) {
//			try {
//				Integer toolInt = Integer.valueOf(loopTool);
//				if (toolInt == eventToolInt) {
//					toolMatchFound = true;
//					break;
//				}
//			} catch(NumberFormatException x) {
		        if(loopTool == null) {
		            toolMatchFound = true;
		            break;
		        } else if(CommonMaterial.isValidSynonym(loopTool)) {
		            if(CommonMaterial.isSynonymFor(loopTool, Material.getMaterial(eventTool))) {
		                toolMatchFound = true;
		                break;
		            } else if(CommonEntity.isValidSynonym(this.original)) {
		                toolMatchFound = true;
		                break;
		    	    }
		        } else {
		            if(loopTool.equalsIgnoreCase(eventTool)) {
		                toolMatchFound = true;
		                break;
		            }
		        }
//			}
	    }
	    
	    if(!toolMatchFound) return false;

	    // Check tool exceptions
	    // Check test case tool exists in array - synonyms here
	    Boolean toolExceptionMatchFound = false;

	    if (this.toolExceptions != null) {
	    	for(String loopTool : this.toolExceptions) {
	    		if(loopTool == null) {
	    			toolExceptionMatchFound = false;
	    			break;
	    		} else if(CommonMaterial.isValidSynonym(loopTool)) {
	    			if(CommonMaterial.isSynonymFor(loopTool, Material.getMaterial(eventTool))) {
	    				toolExceptionMatchFound = true;
	    				break;
	    			} else if(CommonEntity.isValidSynonym(this.original)) {
	    				toolExceptionMatchFound = true;
	    				break;
	    			}
	    		} else {
	    			if(loopTool.equalsIgnoreCase(eventTool)) {
	    				toolExceptionMatchFound = true;
	    				break;
	    			}
	    		}
	    	}
	    }
	    
	    if(toolExceptionMatchFound) return false;
	    
	    // Check worlds
        Boolean worldMatchFound = false;
        
        for(String loopWorld : this.worlds) {
            if(loopWorld == null) {
                worldMatchFound = true;
                break;
            } else {
                if(loopWorld.equalsIgnoreCase(eventWorld.getName())) {
                    worldMatchFound = true;
                    break;
                }
            }
        }
        if(!worldMatchFound) return false;
        
        // Check time
        if (this.time != null && this.time != "null") {
        	String currentTime;
        	if(isDay(eventWorld.getTime())) {
        		currentTime = "DAY";
        	} else {
        		currentTime = "NIGHT";
        	}
        	if (!currentTime.equalsIgnoreCase(this.time)) return false;
        }
        
        
        // Check weather
        Boolean weatherMatchFound = false;
        
        for(String loopWorld : this.weather) {
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
        if(!weatherMatchFound) return false;

        // y-level check
        Boolean heightMatchFound = false;
       if (this.height != null) {
    	   System.out.println(eventHeight+height.substring(0,1)+height.substring(1));
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
       if(!heightMatchFound) return false;
        
        // Biome check
        Boolean biomeMatchFound = false;
        
        for(String loopBiome : this.biome) {
            if(loopBiome == null) {
            	biomeMatchFound = true;
                break;
            } else {
                if(loopBiome.equalsIgnoreCase(biomeName)) {
                	biomeMatchFound = true;
                    break;
                }
            }
        }
        if(!biomeMatchFound) return false;

        // Permissions group check
		Boolean groupMatchFound = false;

		for(String loopGroup : this.permissionGroups) {
			if(loopGroup == null) {
				groupMatchFound = true;
				break;
			} else {

				if (player != null) {
					if (permissionHandler != null) {
						if (permissionHandler.inGroup(eventWorld.getName(), player.getName(), loopGroup)) {
							groupMatchFound = true;
							break;
						}
					}
				}

			}
		}
        if(!groupMatchFound) return false;

        // Permissions group check
		Boolean groupExceptionFound = false;

		for(String loopGroup : this.permissionGroupsExcept) {
			if(loopGroup == null) {
				groupExceptionFound = false;
				break;
			} else {

				if (player != null) {
					if (permissionHandler != null) {
						if (permissionHandler.inGroup(eventWorld.getName(), player.getName(), loopGroup)) {
							groupExceptionFound = true;
							break;
						}
					}
				}

			}
		}
        if(groupExceptionFound) return false;        


        // Permissions check
                Boolean permissionFound = false;

                for(String loopGroup : this.permissions) {
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

        // Permission exceptions check
                Boolean permissionsExceptionFound = false;

                for(String loopGroup : this.permissionsExcept) {
                        if(loopGroup == null) {
                                permissionsExceptionFound = false;
                                break;
                        } else {

                                if (player != null) {
                                        if (permissionHandler != null) {
                                                if (permissionHandler.has(player, "otherblocks.custom."+loopGroup)) {
                                                        permissionsExceptionFound = true;
                                                        break;
                                                }
                                        }
                                }

                        }
                }
        if(permissionsExceptionFound) return false;        
        
        
        // All tests passed - return true.
        return true;
	}
	
	private boolean isDay(long currenttime){
		return ((currenttime % 24000) < 12000 && currenttime > 0 )|| (currenttime < 0 && (currenttime % 24000) < -12000);
	}

}
