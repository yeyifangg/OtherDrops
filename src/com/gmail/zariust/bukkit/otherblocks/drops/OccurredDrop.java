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

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.options.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.DropType;
import com.gmail.zariust.bukkit.otherblocks.options.Height;
import com.gmail.zariust.bukkit.otherblocks.options.Range;
import com.gmail.zariust.bukkit.otherblocks.options.Target;
import com.gmail.zariust.bukkit.otherblocks.options.Time;
import com.gmail.zariust.bukkit.otherblocks.options.Tool;
import com.gmail.zariust.bukkit.otherblocks.options.Weather;
import com.sk89q.worldedit.regions.Region;

public class OccurredDrop extends AbstractDrop
{
	private Location location;
	private Tool tool;
	private World world;
	private List<Region> regions;
	private Weather weather;
	private BlockFace face;
	private Biome biome;
	private long time;
	private int height;
	private double attackRange;
	private int lightLevel;
//	private String dropped;
//	
//	private Range<Short> originalData;
//	private Range<Short> dropData;

	// TODO: Set the tool as well as the other attributes
	public OccurredDrop(BlockBreakEvent evt) {
		super(new Target(evt.getBlock()),Action.BREAK);
		Block block = evt.getBlock();
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(EntityDeathEvent evt) {
		super(new Target(evt.getEntity()),Action.BREAK);
		Entity e = evt.getEntity();
		setLocationWorldBiomeLight(e);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(EntityDamageEvent evt) {
		super(new Target(evt.getEntity()),Action.LEFT_CLICK);
		Entity e = evt.getEntity();
		setLocationWorldBiomeLight(e);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(PaintingBreakEvent evt) {
		super(new Target(evt.getPainting()),Action.BREAK);
		Painting canvas = evt.getPainting();
		setLocationWorldBiomeLight(canvas);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(LeavesDecayEvent evt) {
		// TODO: Actually, shouldn't the target include the block?
		super(Target.LEAF_DECAY,Action.BREAK);
		setLocationWorldBiomeLight(evt.getBlock());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(VehicleDestroyEvent evt) {
		super(new Target(evt.getVehicle()),Action.BREAK);
		setLocationWorldBiomeLight(evt.getVehicle());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getAttacker().getLocation());
	}
	public OccurredDrop(PlayerInteractEvent evt) {
		super(new Target(evt.getClickedBlock()),Action.fromInteract(evt.getAction()));
		setLocationWorldBiomeLight(evt.getClickedBlock());
		face = evt.getBlockFace();
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	public OccurredDrop(PlayerInteractEntityEvent evt) {
		super(new Target(evt.getRightClicked()),Action.RIGHT_CLICK);
		setLocationWorldBiomeLight(evt.getRightClicked());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
	}
	private void setWeatherTimeHeight() {
		weather = Weather.match(biome, world.hasStorm(), world.isThundering());
		time = world.getTime();
		height = location.getBlockY();
	}
	private void setLocationWorldBiomeLight(Block block) {
		location = block.getLocation();
		world = block.getWorld();
		biome = block.getBiome();
		lightLevel = block.getLightLevel();
	}
	private void setLocationWorldBiomeLight(Entity e) {
		location = e.getLocation();
		world = e.getWorld();
		biome = world.getBiome(location.getBlockX(), location.getBlockZ());
		lightLevel = world.getBlockAt(location).getLightLevel();
	}
	
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
		// round up if neccessary
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

	// Attacker Damage
	public int getRandomAttackerDamage()
	{
		if (attackerDamage.getMin() == attackerDamage.getMax()) return attackerDamage.getMin();
		
		int randomVal = (attackerDamage.getMin() + rng.nextInt(attackerDamage.getMax() - attackerDamage.getMin() + 1));
		return randomVal;
	}

	public void setAttackerDamage(int val) {
		attackerDamage = new Range<Integer>(val, val);
	}
	
	public void setAttackerDamage(int low, int high) {
		attackerDamage = new Range<Integer>(low, high);
	}
	
	public boolean isAttackerDamageValid(int test) {
		return attackerDamage.contains(test);
	}
}
