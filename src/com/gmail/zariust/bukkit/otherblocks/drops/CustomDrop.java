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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.options.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.DropType;
import com.gmail.zariust.bukkit.otherblocks.options.Comparative;
import com.gmail.zariust.bukkit.otherblocks.options.Range;
import com.gmail.zariust.bukkit.otherblocks.options.Target;
import com.gmail.zariust.bukkit.otherblocks.options.Time;
import com.gmail.zariust.bukkit.otherblocks.options.Tool;
import com.gmail.zariust.bukkit.otherblocks.options.Weather;

public class CustomDrop extends AbstractDrop
{
	// Conditions
	private List<Tool> tools;
	private List<Tool> toolExceptions;
	private List<World> worlds;
	private Set<String> regions;
	private List<Weather> weather;
	private List<BlockFace> faces;
	private List<BlockFace> facesExcept;
	private List<Biome> biomes;
	private List<Time> times;
	private List<String> permissionGroups; // obseleted - use permissions
	private List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
	private List<String> permissions;
	private List<String> permissionsExcept;
	private Comparative height;
	private Comparative attackRange;
	private Comparative lightLevel;

	@Override
	public boolean matches(AbstractDrop other) {
		if(other instanceof OccurredDrop) {
			OccurredDrop drop = (OccurredDrop) other;
			Entity agent = drop.getAgent();
			if(!isTool(drop.getTool())) return false;
			if(!isWorld(drop.getWorld())) return false;
			if(!isRegion(drop.getRegions())) return false;
			if(!isWeather(drop.getWeather())) return false;
			if(!isBlockFace(drop.getFace())) return false;
			if(!isBiome(drop.getBiome())) return false;
			if(!isTime(drop.getTime())) return false;
			if(!isHeight(drop.getHeight())) return false;
			if(!isAttackInRange((int) drop.getAttackRange())) return false;
			if(!isLightEnough(drop.getLightLevel())) return false;
			if(agent instanceof Player) {
				Player player = (Player) agent;
				if(!inGroup(player)) return false;
				if(!hasPermission(player)) return false;
			}
			return true;
		}
		return false;
	}

	public void setTool(List<Tool> tool) {
		this.tools = tool;
	}

	public List<Tool> getTool() {
		return tools;
	}
	
	public void setToolExceptions(List<Tool> exceptions) {
		this.toolExceptions = exceptions;
	}

	public List<Tool> getToolExceptions() {
		return toolExceptions;
	}

	public boolean isTool(Tool tool) {
		boolean match = false;
		if(tools == null || tools.contains(tool)) match = true;
		if(toolExceptions != null && toolExceptions.contains(tool)) match = false;
		return match;
	}

	public void setWorlds(List<World> places) {
		this.worlds = places;
	}

	public List<World> getWorlds() {
		return worlds;
	}
	
	public boolean isWorld(World world) {
		if(worlds == null) return true;
		return worlds.contains(world);
	}

	public void setRegions(Set<String> areas) {
		this.regions = areas;
	}

	public Set<String> getRegions() {
		return regions;
	}
	
	public boolean isRegion(Set<String> compare) {
		if(regions == null) return true;
		HashSet<String> temp = new HashSet<String>();
		temp.addAll(regions);
		temp.retainAll(compare);
		return !temp.isEmpty();
	}

	public void setWeather(List<Weather> sky) {
		this.weather = sky;
	}

	public List<Weather> getWeather() {
		return weather;
	}
	
	public boolean isWeather(Weather sky) {
		if(weather == null) return true;
		for(Weather type : weather) {
			if(type.matches(sky)) return true;
		}
		return false;
	}

	public void setBlockFace(List<BlockFace> newFaces) {
		this.faces = newFaces;
	}

	public List<BlockFace> getBlockFaces() {
		return faces;
	}
	
	public void setBlockFaceExceptions(List<BlockFace> exceptions) {
		this.facesExcept = exceptions;
	}

	public List<BlockFace> getBlockFaceExceptions() {
		return facesExcept;
	}

	public boolean isBlockFace(BlockFace face) {
		if(face == null) return true;
		boolean match = false;
		if(faces == null || faces.contains(face)) match = true;
		if(facesExcept != null && facesExcept.contains(face)) match = true;
		return match;
	}

	public void setBiome(List<Biome> biome) {
		this.biomes = biome;
	}

	public List<Biome> getBiome() {
		return biomes;
	}
	
	public boolean isBiome(Biome biome) {
		if(biomes == null) return true;
		return biomes.contains(biome);
	}

	public void setTime(List<Time> time) {
		this.times = time;
	}

	public List<Time> getTime() {
		return times;
	}
	
	public boolean isTime(long time) {
		if(times == null) return true;
		for(Time t : times) {
			if(t.contains(time)) return true;
		}
		return false;
	}

	public void setGroups(List<String> newGroups) {
		this.permissionGroups = newGroups;
	}

	public List<String> getGroups() {
		return permissionGroups;
	}
	
	public void setGroupExceptions(List<String> exceptions) {
		this.permissionGroupsExcept = exceptions;
	}

	public List<String> getGroupExceptions() {
		return permissionGroupsExcept;
	}

	public boolean inGroup(Player agent) {
		boolean match = false;
		if(permissionGroups == null) match = true;
		else for(String group : permissionGroups) {
			if(OtherBlocks.permissionHandler.inGroup(agent.getWorld().getName(), agent.getName(), group)) {
				match = true;
				break;
			}
		}
		if(permissionGroupsExcept == null);
		else for(String group : permissionGroupsExcept) {
			if(OtherBlocks.permissionHandler.inGroup(agent.getWorld().getName(), agent.getName(), group)) {
				match = false;
				break;
			}
		}
		return match;
	}

	public void setPermissions(List<String> newPerms) {
		this.permissions = newPerms;
	}

	public List<String> getPermissions() {
		return permissions;
	}
	
	public void setPermissionExceptions(List<String> exceptions) {
		this.permissionsExcept = exceptions;
	}

	public List<String> getPermissionExceptions() {
		return permissionsExcept;
	}

	public boolean hasPermission(Player agent) {
		boolean match = false;
		if(permissions == null) match = true;
		else for(String perm : permissions) {
			if(OtherBlocks.permissionHandler.permission(agent, perm)) {
				match = true;
				break;
			}
		}
		if(permissionsExcept == null);
		else for(String perm : permissionsExcept) {
			if(OtherBlocks.permissionHandler.permission(agent, perm)) {
				match = false;
				break;
			}
		}
		return match;
	}

	public void setHeight(Comparative h) {
		this.height = h;
	}

	public Comparative getHeight() {
		return height;
	}
	
	public boolean isHeight(int h) {
		return height.matches(h);
	}

	public void setAttackRange(Comparative range) {
		this.attackRange = range;
	}

	public Comparative getAttackRange() {
		return attackRange;
	}
	
	public boolean isAttackInRange(int range) {
		return attackRange.matches(range);
	}

	public void setLightLevel(Comparative light) {
		this.lightLevel = light;
	}

	public Comparative getLightLevel() {
		return lightLevel;
	}
	
	public boolean isLightEnough(int light) {
		return lightLevel.matches(light);
	}
	
	// Chance
	private double chance;
	private String exclusiveKey;
	
	public boolean willDrop(Set<String> exclusives) {
		if(exclusives != null && exclusives.contains(exclusiveKey)) return false;
		return rng.nextDouble() > chance / 100.0;
	}
	
	public void setChance(double percent) {
		chance = percent;
	}
	
	public double getChance() {
		return chance;
	}
	
	public void setExclusiveKey(String key) {
		exclusiveKey = key;
	}
	
	public String getExclusiveKey() {
		return exclusiveKey;
	}
	
	// Actions
	private DropType dropped;
	private Range<Float> quantity;
	private Range<Integer> attackerDamage;
	private Range<Short> toolDamage;
	private Range<Integer> delay;
	private double dropSpread;
	private List<Material> replacementBlock;
	private List<DropEvent> event;
	private List<TreeType> eventTrees;
	private List<String> commands;
	private List<String> messages;
//	private Range<Short> originalData;
//	private Range<Short> dropData;
	
	// Constructors TODO: Expand!?
	public CustomDrop(Target targ, Action act) {
		super(targ, act);
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
	
	// Tool Damage
	public short getRandomToolDamage()
	{
		if (toolDamage.getMin() == toolDamage.getMax()) return toolDamage.getMin();
		
		short randomVal = (short) (toolDamage.getMin() + rng.nextInt(toolDamage.getMax() - toolDamage.getMin() + 1));
		return randomVal;
	}

	public void setToolDamage(short val) {
		toolDamage = new Range<Short>(val, val);
	}
	
	public void setToolDamage(short low, short high) {
		toolDamage = new Range<Short>(low, high);
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
//	public String getData() {
//		if (originalData.getMin() == null) {
//			return "";
//		} else if(originalData.getMin() == originalData.getMax()) {
//			return "@" + originalData.getMin();
//		} else {
//			return "@RANGE-" + originalData.getMin() + "-" + originalData.getMax();
//		}
//	}
//	
//	public void setData(short val) {
//		originalData = new Range<Short>(val, val);
//	}
//	
//	public void setData(short low, short high) {
//		originalData = new Range<Short>(low, high);
//	}
//	
//	public boolean isDataValid(short test) {
//		return originalData.contains(test);
//	}

	// DROPData
//	public String getDropDataRange() {
//		if (dropData.getMin() == null) return "";
//		return dropData.getMin().equals(dropData.getMax()) ? dropData.getMin().toString() : dropData.getMin().toString() + "-" + dropData.getMax().toString();
//	}
//
//	public short getRandomDropData()
//	{
//		if (dropData.getMin() == null) return Short.valueOf("0");
//		if (dropData.getMin() == dropData.getMax()) return dropData.getMin();
//		
//		Integer randomVal = (dropData.getMin() + rng.nextInt(dropData.getMax() - dropData.getMin() + 1));
//		Short shortVal = Short.valueOf(randomVal.toString());
//		return shortVal;
//	}
//
//	public void setDropData(Short val) {
//		dropData = new Range<Short>(val, val);
//	}
//	
//	public void setDropData(Short low, Short high) {
//		dropData = new Range<Short>(low, high);
//	}
//	
//	public boolean isDropDataValid(Short test) {
//		return dropData.contains(test);
//	}

	public void setDropped(DropType drop) {
		this.dropped = drop;
	}

	public DropType getDropped() {
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
