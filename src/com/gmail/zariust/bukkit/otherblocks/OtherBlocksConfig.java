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

package com.gmail.zariust.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.material.MaterialData;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.CommonPlugin;
import com.gmail.zariust.bukkit.otherblocks.drops.*;
import com.gmail.zariust.bukkit.otherblocks.droptype.DropType;
import com.gmail.zariust.bukkit.otherblocks.options.*;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.event.DropEventHandler;
import com.gmail.zariust.bukkit.otherblocks.subject.*;

public class OtherBlocksConfig {

	public boolean usePermissions;

	private OtherBlocks parent;

	public boolean dropForBlocks; // this is set to true if config for blocks found
	public boolean dropForCreatures; // this is set to true if config for creatures found
	
	protected int verbosity;
	protected Priority pri;

	public boolean profiling;

	//public static boolean runCommandsSuppressMessage; // if true: "runcommands" responses go to the console rather than the player
	
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;
	protected DropsMap blocksHash;
	
	// Track loaded files so we don't get into an infinite loop
	Set<String> loadedDropFiles = new HashSet<String>();
	
	// Defaults
	private Map<World, Boolean> defaultWorlds;
	private Map<String, Boolean> defaultRegions;
	private Map<Weather, Boolean> defaultWeather;
	private Map<Biome, Boolean> defaultBiomes;
	private Map<Time, Boolean> defaultTime;
	private Map<String, Boolean> defaultPermissionGroups; // obseleted - use permissions
	private Map<String, Boolean> defaultPermissions;
	private Comparative defaultHeight;
	private Comparative defaultAttackRange;
	private Comparative defaultLightLevel;
	
	// A place for events to stash options
	private ConfigurationNode events;

	public OtherBlocksConfig(OtherBlocks instance) {
		parent = instance;
		blocksHash = new DropsMap();

		dropForBlocks = false;
		dropForCreatures = false;
		
		//runCommandsSuppressMessage = true;
		
		verbosity = 2;
		pri = Priority.Lowest;
	}
	
	private void clearDefaults() {
		defaultWorlds = null;
		defaultRegions = null;
		defaultWeather = null;
		defaultBiomes = null;
		defaultTime = null;
		defaultPermissionGroups = null;
		defaultPermissions = null;
		defaultHeight = null;
		defaultAttackRange = null;
		defaultLightLevel = null;
	}

	// load 
	public void load() {
		loadConfig(true);
		parent.setupPermissions(usePermissions);
	}

	public void reload()
	{
		loadConfig(false);
		parent.setupPermissions(usePermissions);
	}

	// Short functions
	//

	public static boolean isCreature(String s) {
		return s.startsWith("CREATURE_");
	}

	public static boolean isPlayer(String s) {
		return s.startsWith("PLAYER");
	}

	public static boolean isPlayerGroup(String s) {
		return s.startsWith("PLAYERGROUP@");
	}

	public static boolean isDamage(String s) {
		return s.startsWith("DAMAGE_");
	}

	public static boolean isSynonymString(String s) {
		return s.startsWith("ANY_");
	}

	public static boolean isLeafDecay(String s) {
		if (s == null) return false;
		return s.startsWith("SPECIAL_LEAFDECAY");
	}

	public static String creatureName(String s) {
		return (isCreature(s) ? s.substring(9) :s);
	}

	public static boolean hasDataEmbedded(String s) {
		return s.contains("@");
	}

	public static String getDataEmbeddedBlockString(String s) {
		if(!hasDataEmbedded(s)) return s;
		return s.substring(0, s.indexOf("@"));
	}

	public static String getDataEmbeddedDataString(String s) {
		if(!hasDataEmbedded(s)) return null;
		return s.substring(s.indexOf("@") + 1);
	}

	// *** DROP EMBEDDED DATA/CHANCE/QUANTITY ***
	public static String getDropEmbeddedChance(String s)  {
		String divider = "/";
		if (s.contains(divider)) {
			for (String section : s.split("/")) {
				if (section.contains("%")) {
					return section.substring(0, section.indexOf("%"));
				}
			}
		} 
		return null;
	}

	public static String getDropEmbeddedQuantity(String s)	{
		String divider = "/";
		if (s.contains(divider)) {
			for (String section : s.split("/")) {
				if (section.matches("[0-9-~]+")) {
					return section;
				}
			}
		} 
		return null;
	}

	// TODO: Do we need the firstRun option?
	public void loadConfig(boolean firstRun)
	{
		blocksHash.clear(); // clear here to avoid issues on /obr reloading
		loadedDropFiles.clear();
		clearDefaults();
		dropForBlocks = false; // reset variable before reading config
		dropForCreatures = false; // reset variable before reading config
		
		File global = new File(parent.getDataFolder(), "otherblocks.yml");
		Configuration globalConfig = new Configuration(global);
		globalConfig.load();

		// TODO: add check here for if otherblocks.yml doesn't exist
		
		// Load in the values from the configuration file
		verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
		pri = CommonPlugin.getConfigPriority(globalConfig);
		enableBlockTo = globalConfig.getBoolean("enableblockto", false);
		usePermissions = globalConfig.getBoolean("usepermissions", false);
		String mainConfigName = globalConfig.getString("rootconfig", "otherblocks-globalconfig");
		events = globalConfig.getNode("events");
		
		// Warn if DAMAGE_WATER is enabled
		if(enableBlockTo) OtherBlocks.logWarning("blockto/damage_water enabled - BE CAREFUL");
		
		loadDropsFile(mainConfigName);
	}

	private void loadDropsFile(String filename) {
		// Check for infinite include loops
		if(loadedDropFiles.contains(filename)) {
			OtherBlocks.logWarning("Infinite include loop detected at " + filename + ".yml");
			return;
		} else loadedDropFiles.add(filename);
		
		File yml = new File(parent.getDataFolder(), filename+".yml");
		Configuration config = new Configuration(yml);
		
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				OtherBlocks.logInfo("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
				config.setProperty("otherblocks", null);
				config.setProperty("include-files", null);
				config.setProperty("defaults", null);
				config.setProperty("aliases", null);
				config.save();
			} catch (IOException ex){
				OtherBlocks.logWarning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
			}
			// Nothing to load in this case, so exit now
			return;
		}
		
		config.load();
		
		// Warn if wrong version
		int configVersion = config.getInt("configversion", 3);
		if(configVersion < 3)
			OtherBlocks.logWarning("config file appears to be in older format; some things may not work");
		else if(configVersion > 3)
			OtherBlocks.logWarning("config file appears to be in newer format; some things may not work");
		
		// Load defaults; each of these functions returns null if the value isn't found
		// TODO: Missing any conditions here? (Apart from tool and action, which are deliberately omitted)
		ConfigurationNode defaults = config.getNode("defaults");
		defaultWorlds = parseWorldsFrom(defaults, null);
		defaultRegions = parseRegionsFrom(defaults, null);
		defaultWeather = Weather.parseFrom(defaults, null);
		defaultBiomes = parseBiomesFrom(defaults, null);
		defaultTime = Time.parseFrom(defaults, null);
		defaultPermissionGroups = parseGroupsFrom(defaults, null);
		defaultPermissions = parsePermissionsFrom(defaults, null);
		defaultHeight = Comparative.parseFrom(defaults, "height", null);
		defaultAttackRange = Comparative.parseFrom(defaults, "attackrange", null);
		defaultLightLevel = Comparative.parseFrom(defaults, "lightlevel", null);
		
		// Load the drops
		List<String> blocks = config.getKeys("otherblocks");
		ConfigurationNode node = config.getNode("otherblocks");
		if (node != null) {
		    for(String blockName : blocks) {
		        Target target = parseTarget(blockName);
		        if(target == null) {
		            OtherBlocks.logWarning("Unrecognized target (skipping): " + blockName);
		            continue;
		        }
		        loadBlockDrops(node, blockName, target);
		    }
		}
		
		// Load the include files
		List<String> includeFiles = config.getStringList("include-files", null);
		for(String include : includeFiles) loadDropsFile(include);
	}

	private void loadBlockDrops(ConfigurationNode node, String blockName, Target target) {
		List<ConfigurationNode> drops = node.getNodeList(blockName, null);
		for(ConfigurationNode dropNode : drops) {
			boolean isGroup = dropNode.getKeys().contains("dropgroup");
			Action action = Action.parseFrom(dropNode);
			if(action == null) {
				OtherBlocks.logWarning("Unrecognized action; skipping");
				continue;
			}
			CustomDrop drop = isGroup ? new DropGroup(target, action) : new SimpleDrop(target, action);
			loadConditions(dropNode, drop);
			if(isGroup) loadDropGroup(dropNode,(DropGroup) drop, target, action);
			else loadSimpleDrop(dropNode, (SimpleDrop) drop);
		}
	}

	private void loadConditions(ConfigurationNode node, CustomDrop drop) {
		// Read tool
		drop.setTool(parseAgentFrom(node));
		// Read faces
		drop.setBlockFace(parseFacesFrom(node));
		
		// Now read the stuff that might have a default; if null is returned, use the default
		drop.setWorlds(parseWorldsFrom(node, defaultWorlds));
		drop.setRegions(parseRegionsFrom(node, defaultRegions));
		drop.setWeather(Weather.parseFrom(node, defaultWeather));
		drop.setBiome(parseBiomesFrom(node, defaultBiomes));
		drop.setTime(Time.parseFrom(node, defaultTime));
		drop.setGroups(parseGroupsFrom(node, defaultPermissionGroups));
		drop.setPermissions(parsePermissionsFrom(node, defaultPermissions));
		drop.setHeight(Comparative.parseFrom(node, "height", defaultHeight));
		drop.setAttackRange(Comparative.parseFrom(node, "attackrange", defaultAttackRange));
		drop.setLightLevel(Comparative.parseFrom(node, "lightlevel", defaultLightLevel));
		
		// Read chance, delay, etc
		drop.setChance(node.getDouble("chance", 100));
		drop.setExclusiveKey(node.getString("exclusive"));
		drop.setDelay(IntRange.parse(node.getString("delay", "0")));
	}

	private void loadSimpleDrop(ConfigurationNode node, SimpleDrop drop) {
		// Read drop
		drop.setDropped(DropType.parseFrom(node));
		String quantityStr = node.getString("quantity");
		if(quantityStr == null) drop.setQuantity(1);
		else drop.setQuantity(DoubleRange.parse(quantityStr));
		// Damage
		drop.setAttackerDamage(IntRange.parse(node.getString("damageattacker", "0")));
		drop.setToolDamage(ShortRange.parse(node.getString("damagetool", "0")));
		// Spread chance
		Object spread = node.getProperty("dropspread");
		if(spread instanceof Boolean) drop.setDropSpread((Boolean) spread);
		else if(spread instanceof Number) drop.setDropSpread(((Number) spread).doubleValue());
		else drop.setDropSpread(true);
		// Replacement block
		drop.setReplacement(parseReplacement(node));
		// Commands, messages, sound effects
		drop.setCommands(getMaybeList(node, "commands"));
		drop.setMessages(getMaybeList(node, "message"));
		drop.setEffects(parseEffectsFrom(node));
		// Events
		List<DropEvent> dropEvents = DropEvent.parseFrom(node);
		if(dropEvents == null) return; // We're done! Note, this means any new options must go above events!
		ListIterator<DropEvent> iter = dropEvents.listIterator();
		while(iter.hasNext()) {
			DropEvent event = iter.next();
			if(!event.canRunFor(drop)) iter.remove();
		}
	}

	private void loadDropGroup(ConfigurationNode node, DropGroup group, Target target, Action action) {
		if(!node.getKeys().contains("drops")) {
			// TODO: Say where the error was
			OtherBlocks.logWarning("Empty drop group; will have no effect!");
			return;
		}
		List<ConfigurationNode> drops = node.getNodeList("drops", null);
		for(ConfigurationNode dropNode : drops) {
			boolean isGroup = node.getKeys().contains("dropgroup");
			if(isGroup) {
				OtherBlocks.logWarning("Drop groups cannot be nested; skipping...");
				continue;
			}
			SimpleDrop drop = new SimpleDrop(target, action);
			loadConditions(dropNode, drop);
			loadSimpleDrop(dropNode, drop);
			group.add(drop);
		}
	}
	
	public static List<String> getMaybeList(ConfigurationNode node, String key) {
		if(node == null) return new ArrayList<String>();
	    Object prop = node.getProperty(key);
		List<String> list;
		if(prop == null) return new ArrayList<String>();
		else if(prop instanceof List) list = node.getStringList(key, null);
		else list = Collections.singletonList(prop.toString());
		return list;
	}

	private MaterialData parseReplacement(ConfigurationNode node) {
		String block = node.getString("replacementblock");
		if(block == null) block = node.getString("replace");
		if(block == null) return null;
		String[] split = block.split("@");
		String name = split[0];
		String data = split.length > 1 ? split[1] : "";
		Material mat = null;
		try {
			mat = Material.getMaterial(Integer.parseInt(name));
		} catch(NumberFormatException e) {
			mat = Material.getMaterial(name.toUpperCase());
		}
		if(mat == null) return null;
		Integer intData = null;
		try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {
			intData = CommonMaterial.parseBlockData(mat, data);
		}
		if(intData == null) return new MaterialData(mat);
		return new MaterialData(mat, intData.byteValue());
		
	}
	
	private Effect parseEffect(String name) {
		// TODO: Effect data and radius?
		try {
			return Effect.valueOf(name);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	private Set<Effect> parseEffectsFrom(ConfigurationNode node) {
		List<String> effects = getMaybeList(node, "effect");
		if(effects.isEmpty()) return null;
		Set<Effect> result = new HashSet<Effect>();
		for(String name : effects) {
			Effect effect = parseEffect(name);
			if(effect == null) {
				OtherBlocks.logWarning("Invalid effect " + name + "; skipping...");
				continue;
			}
			result.add(effect);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private Map<World, Boolean> parseWorldsFrom(ConfigurationNode node, Map<World, Boolean> def) {
		List<String> regions = getMaybeList(node, "world");
		List<String> regionsExcept = getMaybeList(node, "worldexcept");
		if(regions.isEmpty() && regionsExcept.isEmpty()) return def;
		Map<World, Boolean> result = new HashMap<World,Boolean>();
		for(String name : regions) {
			World world = Bukkit.getServer().getWorld(name);
			if(world == null && name.startsWith("-")) {
				world = Bukkit.getServer().getWorld(name.substring(1));
				if(world == null) {
					OtherBlocks.logWarning("Invalid world " + name + "; skipping...");
					continue;
				}
				result.put(world, false);
			} else result.put(world, true);
		}
		for(String name : regionsExcept) {
			World world = Bukkit.getServer().getWorld(name);
			if(world == null) {
				OtherBlocks.logWarning("Invalid world exception " + name + "; skipping...");
				continue;
			}
			result.put(world, false);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private Map<String, Boolean> parseRegionsFrom(ConfigurationNode node, Map<String, Boolean> def) {
		List<String> worlds = getMaybeList(node, "regions");
		List<String> worldsExcept = getMaybeList(node, "regionsexcept");
		if(worlds.isEmpty() && worldsExcept.isEmpty()) return def;
		Map<String, Boolean> result = new HashMap<String,Boolean>();
		for(String name : worlds) {
			if(name.startsWith("-")) {
				result.put(name, false);
			} else result.put(name, true);
		}
		for(String name : worldsExcept) {
			result.put(name, false);
		}
		if(result.isEmpty()) return null;
		return result;
	}
	
	private Biome parseBiome(String name) {
		try {
			return Biome.valueOf(name);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	private Map<Biome, Boolean> parseBiomesFrom(ConfigurationNode node, Map<Biome, Boolean> def) {
		List<String> biomes = getMaybeList(node, "biome");
		if(biomes.isEmpty()) return def;
		HashMap<Biome, Boolean> result = new HashMap<Biome,Boolean>();
		for(String name : biomes) {
			Biome storm = parseBiome(name);
			if(storm == null && name.startsWith("-")) {
				storm = parseBiome(name.substring(1));
				if(storm == null) {
					OtherBlocks.logWarning("Invalid biome " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			} else result.put(storm, true);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private Map<String, Boolean> parseGroupsFrom(ConfigurationNode node, Map<String, Boolean> def) {
		List<String> groups = getMaybeList(node, "permissiongroups");
		List<String> groupsExcept = getMaybeList(node, "permissiongroupsexcept");
		if(groups.isEmpty() && groupsExcept.isEmpty()) return def;
		Map<String, Boolean> result = new HashMap<String,Boolean>();
		for(String name : groups) {
			if(name.startsWith("-")) {
				result.put(name, false);
			} else result.put(name, true);
		}
		for(String name : groupsExcept) {
			result.put(name, false);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private Map<String, Boolean> parsePermissionsFrom(ConfigurationNode node, Map<String, Boolean> def) {
		List<String> permissions = getMaybeList(node, "permissions");
		List<String> permissionsExcept = getMaybeList(node, "permissionsexcept");
		if(permissions.isEmpty() && permissionsExcept.isEmpty()) return def;
		Map<String, Boolean> result = new HashMap<String,Boolean>();
		for(String name : permissions) {
			if(name.startsWith("-")) {
				result.put(name, false);
			} else result.put(name, true);
		}
		for(String name : permissionsExcept) {
			result.put(name, false);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private BlockFace parseFace(String name) {
		try {
			return BlockFace.valueOf(name);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	private Map<BlockFace, Boolean> parseFacesFrom(ConfigurationNode node) {
		List<String> faces = getMaybeList(node, "face");
		if(faces.isEmpty()) return null;
		HashMap<BlockFace, Boolean> result = new HashMap<BlockFace,Boolean>();
		for(String name : faces) {
			BlockFace storm = parseFace(name);
			if(storm == null && name.startsWith("-")) {
				storm = parseFace(name.substring(1));
				if(storm == null) {
					OtherBlocks.logWarning("Invalid block face " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			} else result.put(storm, true);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	public static Map<Agent, Boolean> parseAgentFrom(ConfigurationNode node) {
		List<String> tools = OtherBlocksConfig.getMaybeList(node, "tool");
		List<String> toolsExcept = OtherBlocksConfig.getMaybeList(node, "toolexcept");
		if(tools.isEmpty() && toolsExcept.isEmpty()) return null;
		Map<Agent, Boolean> toolMap = new HashMap<Agent, Boolean>();
		for(String tool : tools) {
			Agent agent = null;
			boolean flag = true;
			if(tool.startsWith("-")) {
				agent = parseAgent(tool.substring(1));
				flag = false;
			} else agent = parseAgent(tool);
			if(agent instanceof MaterialGroupAgent) {
				for(Material mat : ((MaterialGroupAgent) agent).getMaterials())
					toolMap.put(new ToolAgent(mat), flag);
			} else toolMap.put(agent, flag);
		}
		for(String tool : toolsExcept) {
			Agent agent = parseAgent(tool);
			if(agent instanceof MaterialGroupAgent) {
				for(Material mat : ((MaterialGroupAgent) agent).getMaterials())
					toolMap.put(new ToolAgent(mat), false);
			} else toolMap.put(agent, false);
		}
		return toolMap;
	}

	public static Agent parseAgent(String agent) {
		String[] split = agent.split("@");
		String name = split[0].toUpperCase(), data = "";
		if(split.length > 1) data = split[1];
		// Agent can be one of the following
		// - A tool; ie, a Material constant
		// - One of the Material synonyms NOTHING and DYE
		// - A MaterialGroup constant
		// - One of the special wildcards ANY, ANY_CREATURE, ANY_DAMAGE
		// - A DamageCause constant prefixed by DAMAGE_
		//   - DAMAGE_FIRE_TICK and DAMAGE_CUSTOM are valid but not allowed
		//   - DAMAGE_WATER is invalid but allowed, and stored as CUSTOM
		// - A CreatureType constant prefixed by CREATURE_
		// - A projectile; ie a Material constant prefixed by PROJECTILE_
		if(name.startsWith("ANY")) return AnySubject.parseAgent(name);
		else if(name.startsWith("DAMAGE_")) return EnvironmentAgent.parse(name.substring(7), data);
		else if(name.startsWith("CREATURE_")) return CreatureSubject.parse(name.substring(9), data);
		else if(name.startsWith("PROJECTILE_")) return ProjectileAgent.parse(name.substring(11), data);
		else if(name.startsWith("EXPLOSION_")) return ExplosionAgent.parse(name.substring(10), data);
		else return ToolAgent.parse(name, data);
	}

	public static Target parseTarget(String blockName) {
		String[] split = blockName.split("@");
		String name = split[0].toUpperCase(), data = "";
		if(split.length > 1) data = split[1];
		// Name is one of the following:
		// - A Material constant that is a block, painting, or vehicle
		// - A CreatureType constant prefixed by CREATURE_
		// - An integer representing a Material
		// - One of the keywords PLAYER or PLAYERGROUP
		// - A MaterialGroup constant containing blocks
		if(name.equals("PLAYER")) return new PlayerSubject(data);
		else if(name.equals("PLAYERGROUP")) return new GroupSubject(data);
		else if(name.startsWith("ANY_")) return AnySubject.parseTarget(name);
		else if(name.startsWith("CREATURE_")) return CreatureSubject.parse(name, data);
		else return BlockTarget.parse(name, data);
	}

	public ConfigurationNode getEventNode(DropEventHandler event) {
		if(events == null) return null;
		return events.getNode(event.getName());
	}
}