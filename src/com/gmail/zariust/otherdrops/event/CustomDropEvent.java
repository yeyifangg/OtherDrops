// OtherDrops - a Bukkit plugin
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

package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.options.Flag;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.options.Time;
import com.gmail.zariust.otherdrops.options.Weather;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;

public abstract class CustomDropEvent extends AbstractDropEvent implements Runnable
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
	private Set<Flag> flags;
	private Flag.FlagState flagState = new Flag.FlagState();
	private Comparative height;
	private Comparative attackRange;
	private Comparative lightLevel;
	// Chance
	private double chance;
	private String exclusiveKey;
	// Delay
	private IntRange delay;
	// Execution; this is the actual event that this matched
	protected OccurredDropEvent event;

	// Will this drop the default items?
	public abstract boolean isDefault();
	// The name of this drop
	public abstract String getDropName();

	// Conditions
	@Override
	public boolean matches(AbstractDropEvent other) {
		if(!basicMatch(other)) {
			OtherDrops.logInfo("CustomDrop.matches(): basic match failed.", HIGHEST);
			return false;
		}
		if(other instanceof OccurredDropEvent) {
			OccurredDropEvent drop = (OccurredDropEvent) other;
			if(!isTool(drop.getTool()))	return false; // TODO: log message is inside isTool check - do this for all?
			if(!isWorld(drop.getWorld())) {
				OtherDrops.logInfo("CustomDrop.matches(): world match failed.", HIGHEST);
				return false;
			}
			if(!isRegion(drop.getRegions())) {
				OtherDrops.logInfo("CustomDrop.matches(): region match failed.", HIGHEST);
				return false;
			}
			if(!isWeather(drop.getWeather())) {
				OtherDrops.logInfo("CustomDrop.matches(): weather match failed.", HIGHEST);
				return false;
			}
			if(!isBlockFace(drop.getFace())) {
				OtherDrops.logInfo("CustomDrop.matches(): blockface match failed.", HIGHEST);
				return false;
			}
			if(!isBiome(drop.getBiome())) {
				OtherDrops.logInfo("CustomDrop.matches(): biome match failed.", HIGHEST);
				return false;
			}
			if(!isTime(drop.getTime())) {
				OtherDrops.logInfo("CustomDrop.matches(): time match failed.", HIGHEST);
				return false;
			}
			if(!isHeight(drop.getHeight())) {
				OtherDrops.logInfo("CustomDrop.matches(): height match failed.", HIGHEST);
				return false;
			}
			if(!isAttackInRange((int) drop.getAttackRange())) {
				OtherDrops.logInfo("CustomDrop.matches(): range match failed.", HIGHEST);
				return false;
			}
			if(!isLightEnough(drop.getLightLevel())) {
				OtherDrops.logInfo("CustomDrop.matches(): lightlevel match failed.", HIGHEST);
				return false;
			}
			if(drop.getTool() instanceof PlayerSubject) {
				Player player = ((PlayerSubject) drop.getTool()).getPlayer();
				if(!inGroup(player)) {
					OtherDrops.logInfo("CustomDrop.matches(): player group match failed.", HIGHEST);
					return false;
				}
				if(!hasPermission(player)) {
					OtherDrops.logInfo("CustomDrop.matches(): player permission match failed.", HIGHEST);
					return false;
				}
			}
			if(!checkFlags(drop)) {
				OtherDrops.logInfo("CustomDrop.matches(): a flag match failed.", HIGHEST);
				return false;
			}
			return true;
		}
		
		OtherDrops.logInfo("CustomDrop.matches(): match failed - not an OccuredEvent?", HIGHEST);
		return false;
	}

	public void setTool(Map<Agent, Boolean> tool) {
		this.tools = tool;
	}

	public Map<Agent, Boolean> getTool() {
		return tools;
	}
	
	public String getToolString() {
		return mapToString(tools);
	}
	// TODO:
	//Actually, if it's null it means that the tool appeared neither as a tool nor as a toolexcept, while if it's false it appeared as a toolexcept and if it's true as a tool.
	//A better example to try (and make sure it works) is something like [ANY_SPADE, -IRON_SPADE].
	public boolean isTool(Agent tool) {
		boolean positiveMatch = false;
		if(tools == null) return true;
		// tools={DIAMOND_SPADE@=true}
		// tool=PLAYER@Xarqn with DIAMOND_SPADE@4
		// Note: tools.get(tool) fails with a player.
		
		// Check for tool matches
		for(Map.Entry<Agent,Boolean> agent : tools.entrySet()) {
			if(!agent.getValue()) continue;
			if(agent.getKey().matches(tool)) {
				positiveMatch = true;
				break;
			}
		}
		
		// Check for tool exception matches
		for(Map.Entry<Agent,Boolean> agent : tools.entrySet()) {
			if(agent.getValue()) continue;
			if(agent.getKey().matches(tool)) {
				positiveMatch = false;
				break;
			}
		}
		//TODO: somewhere in here check if the tool is a player and if there's not a match for PLAYER check the tool the player is holding
		OtherDrops.logInfo("Tool match = "+positiveMatch+" - tool="+String.valueOf(tool)+" tools="+tools.toString(), HIGHEST);
		return positiveMatch;
	}

	public void setWorlds(Map<World, Boolean> places) {
		this.worlds = places;
	}

	public Map<World, Boolean> getWorlds() {
		return worlds;
	}
	
	public String getWorldsString() {
		return mapToString(worlds);
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
	
	public String getRegionsString() {
		return mapToString(regions);
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
	
	public String getWeatherString() {
		return mapToString(weather);
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
	
	public String getBlockFacesString() {
		return mapToString(faces);
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
	
	public String getBiomeString() {
		return mapToString(biomes);
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
	
	public String getTimeString() {
		return mapToString(times);
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
	
	public String getGroupsString() {
		return mapToString(permissionGroups);
	}

	public boolean inGroup(Player agent) {
		if(permissionGroups == null) return true;
		boolean match = false;
		for(String group : permissionGroups.keySet()) {
			if(OtherDrops.inGroup(agent, group)) {
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
	
	public String getPermissionsString() {
		return mapToString(permissions);
	}

	public boolean hasPermission(Player agent) {
		if(permissions == null) return true;
		boolean match = false;
		for(String perm : permissions.keySet()) {
			if(OtherDrops.plugin.hasPermission(agent, "otherdrops.custom."+perm)) {
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
		if (height == null) return true;
		return height.matches(h);
	}

	public void setAttackRange(Comparative range) {
		this.attackRange = range;
	}

	public Comparative getAttackRange() {
		return attackRange;
	}
	
	public boolean isAttackInRange(int range) {
		if (attackRange == null) return true;
		return attackRange.matches(range);
	}

	public void setLightLevel(Comparative light) {
		this.lightLevel = light;
	}

	public Comparative getLightLevel() {
		return lightLevel;
	}
	
	public boolean isLightEnough(int light) {
		if (lightLevel == null) return true;
		return lightLevel.matches(light);
	}
	
	public void setFlags(Set<Flag> newFlags) {
		flags = newFlags;
	}
	
	public void setFlag(Flag flag) {
		if(flags == null) setFlags(new HashSet<Flag>());
		flags.add(flag);
	}
	
	public boolean hasFlag(Flag flag) {
		if(flags == null) setFlags(new HashSet<Flag>());
		return flags.contains(flag);
	}
	
	public void unsetFlag(Flag flag) {
		if(flags == null) setFlags(new HashSet<Flag>());
		flags.remove(flag);
	}
	
	public Flag.FlagState getFlagState() {
		return flagState;
	}
	
	public boolean checkFlags(OccurredDropEvent drop) {
		boolean shouldDrop = true;
		for(Flag flag : Flag.values()) {
			flag.matches(drop, flags.contains(flag), flagState);
			shouldDrop = shouldDrop && flagState.dropThis;
		}
		return shouldDrop;
	}
	
	// Chance
	public boolean willDrop(Set<String> exclusives) {
		if(exclusives != null) {
			if(exclusives.contains(exclusiveKey)) {
				OtherDrops.logInfo("Drop failed due to exclusive key.",HIGHEST);
				return false;
			}
			if(exclusiveKey != null) exclusives.add(exclusiveKey);
		}
		// TODO: not as elegant as the single liner but needed for debugging
		Double rolledValue = rng.nextDouble();
		boolean chancePassed = rolledValue <= chance / 100.0; 
		if (chancePassed) {
			return true;
		} else {
			OtherDrops.logInfo("Drop failed due to chance ("+String.valueOf(chance)+", rolled: "+rolledValue*100+")",HIGHEST);
			return false;
		}
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
	
	protected CustomDropEvent(Target targ, Action act) {
		super(targ, act);
	}
	
	// Delay
	public int getRandomDelay()
	{
		if (delay.getMin() == delay.getMax()) return delay.getMin();
		
		int randomVal = (delay.getMin() + rng.nextInt(delay.getMax() - delay.getMin() + 1));
		return randomVal;
	}
	
	public String getDelayRange() {
		return delay.getMin().equals(delay.getMax()) ? delay.getMin().toString() : delay.getMin().toString() + "-" + delay.getMax().toString();
	}

	public void setDelay(IntRange val) {
		delay = val;
	}

	public void setDelay(int val) {
		delay = new IntRange(val, val);
	}
	
	public void setDelay(int low, int high) {
		delay = new IntRange(low, high);
	}
	
	public void perform(OccurredDropEvent evt) {
		event = evt;
		int schedule = getRandomDelay();
		if(schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, this, schedule);
		else run();
	}
	
	private String setToString(Set<?> set) {
		if(set.size() > 1) return set.toString();
		if(set.isEmpty()) return "(any/none)";
		List<Object> list = new ArrayList<Object>();
		list.addAll(set);
		if (list.get(0) == null) {
			OtherDrops.logWarning("CustomDropEvent.setToString - list.get(0) is null?", Verbosity.HIGHEST);
			return "";
		}
		return list.get(0).toString();
	}
	
	private String mapToString(Map<?, Boolean> map) {
		return (map == null) ? null : setToString(stripFalse(map));
	}
	
	private Set<?> stripFalse(Map<?, Boolean> map) {
		Set<Object> set = new HashSet<Object>();
		for(Object key : map.keySet()) {
			if(map.get(key)) set.add(key);
		}
		return set;
	}
	
	@Override
	public String getLogMessage() {
		StringBuilder log = new StringBuilder();
		log.append(toString() + ": ");
		// Tool
		log.append(mapToString(tools));
		// Faces
		if(faces != null) log.append(" on faces " + mapToString(faces));
		// Placeholder for drops info
		log.append(" now drops %d");
		// Chance
		log.append(" with " + Double.toString(chance) + "% chance");
		// Worlds and regions
		if(worlds != null) {
			log.append(" in worlds " + mapToString(worlds));
			if(regions != null) log.append(" and regions " + mapToString(regions));
		} else if(regions != null) log.append(" in regions " + mapToString(regions));
		// Other conditions
		if(weather != null || biomes != null || times != null || height != null || attackRange != null || lightLevel != null) {
			log.append(" with conditions");
			char sep = ':';
			if(weather != null) {
				log.append(sep + " " + mapToString(weather));
				sep = ',';
			}
			if(biomes != null) {
				log.append(sep + " " + mapToString(biomes));
				sep = ',';
			}
			if(times != null) {
				log.append(sep + " " + mapToString(times));
				sep = ',';
			}
			if(height != null) {
				log.append(sep + " " + height.toString());
				sep = ',';
			}
			if(attackRange != null) {
				log.append(sep + " " + attackRange.toString());
				sep = ',';
			}
			if(lightLevel != null) {
				log.append(sep + " " + lightLevel.toString());
				sep = ',';
			}
		}
		return log.toString();
	}
}
