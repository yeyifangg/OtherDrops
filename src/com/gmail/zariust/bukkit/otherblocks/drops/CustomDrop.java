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
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Comparative;
import com.gmail.zariust.bukkit.otherblocks.options.IntRange;
import com.gmail.zariust.bukkit.otherblocks.options.Range;
import com.gmail.zariust.bukkit.otherblocks.options.Time;
import com.gmail.zariust.bukkit.otherblocks.options.Weather;
import com.gmail.zariust.bukkit.otherblocks.options.action.Action;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;
import com.gmail.zariust.bukkit.otherblocks.options.tool.Agent;

public abstract class CustomDrop extends AbstractDrop implements Runnable
{
	// Conditions
	private Map<Agent, Boolean> tools;
	private Map<World, Boolean> worlds;
	private Map<String, Boolean> regions;
	private Map<Weather, Boolean> weather;
	private Map<BlockFace, Boolean> faces;
	private Map<Biome, Boolean> biomes;
	private Map<Time, Boolean> times;
	private Map<String, Boolean> permissionGroups; // obseleted - use permissions
	private Map<String, Boolean> permissions;
	private Comparative height;
	private Comparative attackRange;
	private Comparative lightLevel;

	@Override
	public boolean matches(AbstractDrop other) {
		if(!basicMatch(other)) return false;
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

	public void setTool(Map<Agent, Boolean> tool) {
		this.tools = tool;
	}

	public Map<Agent, Boolean> getTool() {
		return tools;
	}

	public boolean isTool(Agent tool) {
		// TODO: Correctly handle "wildcard" tools
		boolean match = false;
		if(tools == null) return true;
		else for(Agent agent : tools.keySet()) {
			if(agent.matches(tool)) {
				if(!tools.get(tool)) return false;
				else match = true;
			}
		}
		return match;
	}

	public void setWorlds(Map<World, Boolean> places) {
		this.worlds = places;
	}

	public Map<World, Boolean> getWorlds() {
		return worlds;
	}
	
	public boolean isWorld(World world) {
		boolean match = false;
		if(worlds == null) match = true;
		else if(worlds.containsKey(world)) match = worlds.get(world);
		return match;
	}

	public void setRegions(Map<String, Boolean> areas) {
		this.regions = areas;
	}

	public Map<String, Boolean> getRegions() {
		return regions;
	}
	
	public boolean isRegion(Set<String> compare) {
		if(regions == null) return true;
		HashSet<String> temp = new HashSet<String>();
		temp.addAll(regions.keySet());
		temp.retainAll(compare);
		if(temp.isEmpty()) return false;
		for(String region : temp) {
			// Exclusions override allowed regions
			if(!regions.get(region)) return false;
		}
		return true;
	}

	public void setWeather(Map<Weather, Boolean> sky) {
		this.weather = sky;
	}

	public Map<Weather, Boolean> getWeather() {
		return weather;
	}
	
	public boolean isWeather(Weather sky) {
		if(weather == null) return true;
		boolean match = false;
		for(Weather type : weather.keySet()) {
			if(type.matches(sky)) {
				if(weather.get(type)) match = true;
				else return false;
			}
		}
		return match;
	}

	public void setBlockFace(Map<BlockFace, Boolean> newFaces) {
		this.faces = newFaces;
	}

	public Map<BlockFace, Boolean> getBlockFaces() {
		return faces;
	}

	public boolean isBlockFace(BlockFace face) {
		if(face == null) return true;
		boolean match = false;
		if(faces == null) match = true;
		else if(faces.containsKey(face)) match = faces.get(face);
		return match;
	}

	public void setBiome(Map<Biome, Boolean> biome) {
		this.biomes = biome;
	}

	public Map<Biome, Boolean> getBiome() {
		return biomes;
	}
	
	public boolean isBiome(Biome biome) {
		boolean match = false;
		if(biomes == null) match = true;
		else if(biomes.containsKey(biome)) match = biomes.get(biome);
		return match;
	}

	public void setTime(Map<Time, Boolean> time) {
		this.times = time;
	}

	public Map<Time, Boolean> getTime() {
		return times;
	}
	
	public boolean isTime(long time) {
		if(times == null) return true;
		boolean match = false;
		for(Time t : times.keySet()) {
			if(t.contains(time)) {
				if(times.get(t)) match = true;
				else return false;
			}
		}
		return match;
	}

	public void setGroups(Map<String, Boolean> newGroups) {
		this.permissionGroups = newGroups;
	}

	public Map<String, Boolean> getGroups() {
		return permissionGroups;
	}

	public boolean inGroup(Player agent) {
		if(permissionGroups == null) return true;
		boolean match = false;
		for(String group : permissionGroups.keySet()) {
			if(OtherBlocks.permissionHandler.inGroup(agent.getWorld().getName(), agent.getName(), group)) {
				if(permissionGroups.get(group)) match = true;
				else return false;
			}
		}
		return match;
	}

	public void setPermissions(Map<String, Boolean> newPerms) {
		this.permissions = newPerms;
	}

	public Map<String, Boolean> getPermissions() {
		return permissions;
	}

	public boolean hasPermission(Player agent) {
		if(permissions == null) return true;
		boolean match = false;
		for(String perm : permissions.keySet()) {
			if(OtherBlocks.permissionHandler.permission(agent, perm)) {
				if(permissions.get(perm)) match = true;
				else return false;
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
	
	protected CustomDrop(Target targ, Action act) {
		super(targ, act);
	}
	
	// Delay
	private Range<Integer> delay;
	protected OccurredDrop event;
	
	public int getRandomDelay()
	{
		if (delay.getMin() == delay.getMax()) return delay.getMin();
		
		int randomVal = (delay.getMin() + rng.nextInt(delay.getMax() - delay.getMin() + 1));
		return randomVal;
	}
	
	public String getDelayRange() {
		return delay.getMin().equals(delay.getMax()) ? delay.getMin().toString() : delay.getMin().toString() + "-" + delay.getMax().toString();
	}

	public void setDelay(int val) {
		delay = new IntRange(val, val);
	}
	
	public void setDelay(int low, int high) {
		delay = new IntRange(low, high);
	}
	
	public void perform(OccurredDrop evt) {
		event = evt;
		int schedule = getRandomDelay();
		if(schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherBlocks.plugin, this, schedule);
		else run();
	}
}
