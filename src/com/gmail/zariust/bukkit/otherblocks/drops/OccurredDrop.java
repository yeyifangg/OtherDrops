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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.bukkit.otherblocks.drops;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Range;

public class OccurredDrop extends AbstractDrop
{	
	private String dropped;
	private double dropSpread;
	private Range<Integer> delay;
	
	private Range<Short> originalData;
	private Range<Short> dropData;
	private Range<Float> quantity;
	
	// Delay
	public int getRandomDelay()
	{
		if (delay.getMin() == delay.getMax()) return delay.getMin();
		
		int randomVal = (delay.getMin() + rng.nextInt(delay.getMax() - delay.getMin() + 1));
		return randomVal;
	}

	public void setDelay(int val) {
		delay = new Range<Integer>(val, val);
	}
	
	public void setDelay(int low, int high) {
		delay = new Range<Integer>(low, high);
	}
	
	// Quantity getters and setters


	public int getRandomQuantityInt() {
		double random = getRandomQuantityDouble();
		int intPart = (int) random;
		// .intValue() discards the decimal place - round up if neccessary
		if (random - intPart >= 0.5) {
				intPart = intPart + 1;
		}
		return intPart;
	}

	public double getRandomQuantityDouble() {
		//TODO: fix this function so we don't need to multiply by 100
		// this will cause an error if the number is almost max float
		// but a drop that high would crash the server anyway
		float min = (quantity.getMin() * 100);
		float max = (quantity.getMax() * 100);
		int val = (int)min + rng.nextInt((int)max - (int)min + 1);
		double doubleVal = Double.valueOf(val); 
		double deciVal = doubleVal/100;
		return deciVal;
	}
	
	public String getQuantityRange() {
		return quantity.getMin().equals(quantity.getMax()) ? quantity.getMin().toString() : quantity.getMin().toString() + "-" + quantity.getMax().toString();
	}
	
	public void setQuantity(float val) {
		quantity = new Range<Float>(val, val);
	}
	
	public void setQuantity(float low, float high) {
		quantity = new Range<Float>(low, high);
	}
	
	// Data getters and setters
	public String getData() {
		if (originalData.getMin() == null) {
			return "";
		} else if(originalData.getMin() == originalData.getMax()) {
			return "@" + originalData.getMin();
		} else {
			return "@RANGE-" + originalData.getMin() + "-" + originalData.getMax();
		}
	}
	
	public void setData(short val) {
		originalData = new Range<Short>(val, val);
	}
	
	public void setData(short low, short high) {
		originalData = new Range<Short>(low, high);
	}
	
	public boolean isDataValid(short test) {
		return originalData.contains(test);
	}

	// DROPData
	public String getDropDataRange() {
		if (dropData.getMin() == null) return "";
		return dropData.getMin().equals(dropData.getMax()) ? dropData.getMin().toString() : dropData.getMin().toString() + "-" + dropData.getMax().toString();
	}

	public short getRandomDropData()
	{
		if (dropData.getMin() == null) return Short.valueOf("0");
		if (dropData.getMin() == dropData.getMax()) return dropData.getMin();
		
		Integer randomVal = (dropData.getMin() + rng.nextInt(dropData.getMax() - dropData.getMin() + 1));
		Short shortVal = Short.valueOf(randomVal.toString());
		return shortVal;
	}

	public void setDropData(Short val) {
		dropData = new Range<Short>(val, val);
	}
	
	public void setDropData(Short low, Short high) {
		dropData = new Range<Short>(low, high);
	}
	
	public boolean isDropDataValid(Short test) {
		return dropData.contains(test);
	}

	public void setDropped(String drop) {
		this.dropped = drop;
	}

	public String getDropped() {
		return dropped;
	}

	public void setDropSpread(Double spread) {
		this.dropSpread = spread;
	}

	public double getDropSpread() {
		return dropSpread;
	}

}
