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
import org.bukkit.entity.CreatureType;

import com.sargant.bukkit.common.CommonEntity;
import com.sargant.bukkit.common.CommonMaterial;

public class OtherBlocksContainer
{
	public String original;
	public String dropped;
	public List<String> tool;
	public List<String> worlds;
	public Integer damage;
	public Double chance;
	public Short color;
	public List<String> messages;
	public String time;
	public List<String> weather;
	public List<String> biome;
	public List<String> event;
	public String height;
	
	private Short originalDataMin;
    private Short originalDataMax;
	private Integer quantityMin;
    private Integer quantityMax;
	
	private static Random rng = new Random();

	// Quantity getters and setters
	
	public Integer getRandomQuantity() {
	    return (quantityMin + rng.nextInt(quantityMax - quantityMin + 1));
	}
	
	public String getQuantityRange() {
	    return (quantityMin.equals(quantityMax) ? quantityMin.toString() : quantityMin.toString() + "-" + quantityMax.toString());
	}
	
	public void setQuantity(Integer val) {
	    try {
 	        this.setQuantity(val, val);
	    } catch(NullPointerException x) {
	        this.quantityMin = this.quantityMax = 1;
	    }
	}
	
	public void setQuantity(Integer low, Integer high) {
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
	
	// Comparison tests
	public boolean compareTo(String eventTarget, Short eventData, String eventTool, World eventWorld) {
	    // Check original block - synonyms here
	    if(CommonMaterial.isValidSynonym(this.original)) {
	        if(!CommonMaterial.isSynonymFor(this.original, Material.getMaterial(eventTarget))) return false;
	    } else if(CommonEntity.isValidSynonym(this.original)) {
	        if(!CommonEntity.isSynonymFor(this.original, CreatureType.fromName(eventTarget))) return false;
	    } else {
	        if(!this.original.equalsIgnoreCase(eventTarget)) return false;
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
	    }
	    
	    if(!toolMatchFound) return false;
	    
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
	    
        // All tests passed - return true.
        return true;
	}
	
	private boolean isDay(long currenttime){
		return ((currenttime % 24000) < 12000 && currenttime > 0 )|| (currenttime < 0 && (currenttime % 24000) < -12000);
	}

}
