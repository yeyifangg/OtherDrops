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

public class OB_Drop extends AbstractDrop
{	
	public String dropped;
	public Double dropSpread;
    public Integer delayMin;
    public Integer delayMax;
    
	private Short originalDataMin;
    private Short originalDataMax;
    private Short dropDataMin;
    private Short dropDataMax;
	private Float quantityMin;
    private Float quantityMax;

	
	
	// Delay
	public Integer getRandomDelay()
	{
		if (delayMin == delayMax) return delayMin;
		
		Integer randomVal = (delayMin + rng.nextInt(delayMax - delayMin + 1));
		return randomVal;
	}

	public void setDelay(Integer val) {
	    try {
	        this.setDelay(val, val);
	    } catch(NullPointerException x) {
	        this.delayMin = this.delayMax = null;
	    }
	}
	
	public void setDelay(Integer low, Integer high) {
	    if(low < high) {
	        this.delayMin = low;
	        this.delayMax = high;
	    } else {
	        this.delayMin = high;
	        this.delayMax = low;
	    }
	}
	
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

}
