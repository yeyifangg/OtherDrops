package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zarius.common.CommonEntity;
import com.gmail.zarius.common.CommonMaterial;
import com.nijiko.permissions.PermissionHandler;

public class OBContainer_Drops {
	public List<OB_Drop> list = null;

    public OBContainer_Drops() {
    	list = new ArrayList<OB_Drop>();
    }

    public String name;
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
    
    private Integer attackerDamageMin;
    private Integer attackerDamageMax;    
	
	private static Random rng = new Random();

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
		
		Entity eventEntity = null;
		Player eventPlayer = null;
		Block eventBlock = null;
		Integer eventHeight = null;
		String biomeName = null;
		
		if (eventObject instanceof Block) {
			eventBlock = (Block) eventObject;
			eventHeight = eventBlock.getY();
			biomeName = eventBlock.getBiome().name();
		} else if (eventObject instanceof Player) {
			eventPlayer = (Player) eventObject;
			eventHeight = eventPlayer.getLocation().getBlockY();
			biomeName = eventPlayer.getLocation().getBlock().getBiome().name();
		} else if (eventObject instanceof Entity) {
			eventEntity = (Entity) eventObject;
			
			eventHeight = eventEntity.getLocation().getBlock().getY();
			biomeName = eventEntity.getLocation().getBlock().getBiome().name();
		}

		// Check original block - synonyms here
		// NOTE: don't need to do this here, checked earlier when grabbing hash map

	    
		// if this.worlds == null then nothing has been added, skip everything and return true
		if (this.worlds == null)
			return true;
		

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
        if (this.time != null) {
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
