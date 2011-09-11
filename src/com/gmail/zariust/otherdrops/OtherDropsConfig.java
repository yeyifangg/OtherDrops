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

package com.gmail.zariust.otherdrops;

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
import org.bukkit.entity.CreatureType;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import com.gmail.zariust.common.CommonPlugin;
import com.gmail.zariust.common.Verbosity;
import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SimpleData;
import com.gmail.zariust.otherdrops.event.*;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.ItemDrop;
import com.gmail.zariust.otherdrops.options.*;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.special.SpecialResultHandler;
import com.gmail.zariust.otherdrops.special.SpecialResultLoader;
import com.gmail.zariust.otherdrops.subject.*;

public class OtherDropsConfig {

	public boolean usePermissions;

	private OtherDrops parent;

	public boolean dropForBlocks; // this is set to true if config for blocks found
	public boolean dropForCreatures; // this is set to true if config for creatures found
	public boolean dropForExplosions;
	
	protected Verbosity verbosity;
	protected Priority pri;

	public boolean profiling;
	
	public boolean enableBlockTo;
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
	private Map<String, Boolean> defaultPermissionGroups;
	private Map<String, Boolean> defaultPermissions;
	private Comparative defaultHeight;
	private Comparative defaultAttackRange;
	private Comparative defaultLightLevel;
	private Action defaultAction;
	
	// A place for special events to stash options
	private ConfigurationNode events;

	public OtherDropsConfig(OtherDrops instance) {
		parent = instance;
		blocksHash = new DropsMap();

		dropForBlocks = false;
		dropForCreatures = false;
		
		//runCommandsSuppressMessage = true;
		
		verbosity = NORMAL;
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
		loadConfig();
		parent.setupPermissions(usePermissions);
	}
	
	public void loadConfig()
	{
		blocksHash.clear(); // clear here to avoid issues on /obr reloading
		loadedDropFiles.clear();
		clearDefaults();
		dropForBlocks = false; // reset variable before reading config
		dropForCreatures = false; // reset variable before reading config
		
		String filename = "otherdrops-config.yml";
		if (!(new File(parent.getDataFolder(), filename).exists())) filename = "otherblocks-globalconfig.yml"; // Compatibility with old filename
		if (!(new File(parent.getDataFolder(), filename).exists())) filename = "otherdrops-config.yml";  // If old file not found, go back to new name
		
		File global = new File(parent.getDataFolder(), filename);
		Configuration globalConfig = new Configuration(global);
		
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!global.exists()) {
			try {
				global.createNewFile();
				OtherDrops.logInfo("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
				globalConfig.setProperty("verbosity", "normal");
				globalConfig.setProperty("priority", "high");
				globalConfig.setProperty("usepermissions", true);
				globalConfig.save();
			} catch (IOException ex){
				OtherDrops.logWarning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
			}
		}

		// Load in the values from the configuration file
		globalConfig.load();
		String configKeys = globalConfig.getKeys().toString();
		verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
		pri = CommonPlugin.getConfigPriority(globalConfig);
		enableBlockTo = globalConfig.getBoolean("enableblockto", false);
		usePermissions = globalConfig.getBoolean("usepermissions", false);
		String mainDropsName = globalConfig.getString("rootconfig", "otherdrops-drops.yml");
		if (!(new File(parent.getDataFolder(), mainDropsName).exists())) mainDropsName = "otherblocks-globalconfig.yml"; // Compatibility with old filename
		if (!(new File(parent.getDataFolder(), mainDropsName).exists())) mainDropsName = "otherdrops-drops.yml";         // If old file not found, go back to new name

		events = globalConfig.getNode("events");
		if(events == null) {
			globalConfig.setProperty("events", new HashMap<String,Object>());
			events = globalConfig.getNode("events");
			if(events == null) OtherDrops.logWarning("EVENTS ARE NULL");
			else OtherDrops.logInfo("Events node created.");
		}
		
		// Warn if DAMAGE_WATER is enabled
		if(enableBlockTo) OtherDrops.logWarning("blockto/damage_water enabled - BE CAREFUL");

		try {
			SpecialResultLoader.loadEvents();
		} catch (Exception except) {
			OtherDrops.logWarning("Event files failed to load - this shouldn't happen, please inform developer.");
			if(verbosity.exceeds(HIGHEST)) except.printStackTrace();
		}

		OtherDrops.logInfo("Loaded global config ("+global+"), keys found: "+configKeys + " (verbosity="+verbosity+")", Verbosity.HIGH);

		loadDropsFile(mainDropsName);
	}

	private void loadDropsFile(String filename) {
		// Check for infinite include loops
		if(loadedDropFiles.contains(filename)) {
			OtherDrops.logWarning("Infinite include loop detected at " + filename);
			return;
		} else loadedDropFiles.add(filename);
		
		OtherDrops.logInfo("Loading file: "+filename,HIGH);
		
		File yml = new File(parent.getDataFolder(), filename);
		Configuration config = new Configuration(yml);
		
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				OtherDrops.logInfo("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
				config.setProperty("otherdrops", null);
				config.setProperty("include-files", null);
				config.setProperty("defaults", null);
				config.setProperty("aliases", null);
				config.save();
			} catch (IOException ex){
				OtherDrops.logWarning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
			}
			// Nothing to load in this case, so exit now
			return;
		}
		
		config.load();
		
		// Warn if wrong version
		int configVersion = config.getInt("configversion", 3);
		if(configVersion < 3)
			OtherDrops.logWarning("config file appears to be in older format; some things may not work");
		else if(configVersion > 3)
			OtherDrops.logWarning("config file appears to be in newer format; some things may not work");
		
		// Load defaults; each of these functions returns null if the value isn't found
		ConfigurationNode defaults = config.getNode("defaults");

		// Check for null - it's possible that the defaults key doesn't exist or is empty
		if (defaults != null) {
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
			defaultAction = Action.parseFrom(defaults, Action.BREAK);
		}
			
		// Load the drops
		List<String> blocks = config.getKeys("otherdrops");
		ConfigurationNode node = config.getNode("otherdrops");
		if(node == null) { // Compatibility
			blocks = config.getKeys("otherblocks");
			node = config.getNode("otherblocks");
		}
		if (node != null) {
		    for(String blockName : blocks) {
		        Target target = parseTarget(blockName);
		        if(target == null) {
		            OtherDrops.logWarning("Unrecognized target (skipping): " + blockName);
		            continue;
		        }
		        switch(target.getType()) {
		        case BLOCK: dropForBlocks = true; break;
		        case PLAYER:
		        case CREATURE: dropForCreatures = true; break;
		        case EXPLOSION: dropForExplosions = true; break;
		        default:
		        	// If you want to have other similar flags, add them above the default
		        	// Possibilities are DAMAGE, PROJECTILE, SPECIAL (but special isn't used for anything)
		        	// (The default is here so I don't get an "incomplete switch" warning.)
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
			Action action = Action.parseFrom(dropNode, defaultAction);
			if(action == null) {
				OtherDrops.logWarning("Unrecognized action; skipping (valid actions: "+Action.getValidActions().toString()+")",NORMAL);
				continue;
			}
			if (blockName.equalsIgnoreCase("SPECIAL_LEAFDECAY")) action = Action.LEAF_DECAY; // for compatibility
			CustomDropEvent drop = isGroup ? new GroupDropEvent(target, action) : new SimpleDropEvent(target, action);
			loadConditions(dropNode, drop);
			if(isGroup) loadDropGroup(dropNode,(GroupDropEvent) drop, target, action);
			else loadSimpleDrop(dropNode, (SimpleDropEvent) drop);

			if (drop.getTool() == null || drop.getTool().isEmpty()) {
				OtherDrops.logWarning("Unrecognized tool; skipping.",NORMAL); // FIXME: need to report the drop or tool details so we know what one is wrong
				continue;
			}
			blocksHash.addDrop(drop);
		}
	}

	private void loadConditions(ConfigurationNode node, CustomDropEvent drop) {
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
		drop.setDropInsideMobArena(parseDropInMobArenaFrom(node));
		
		// Read chance, delay, etc
		drop.setChance(parseChanceFrom(node));
		Object exclusive = node.getProperty("exclusive");
		if(exclusive != null) drop.setExclusiveKey(exclusive.toString());
		drop.setDelay(IntRange.parse(node.getString("delay", "0")));
	}

	public static double parseChanceFrom(ConfigurationNode node) {
		String chanceString = node.getString("chance", null);
		double chance = 100;
		if (chanceString == null) {
			chance = 100;
		} else {
			try { 
				chance = Double.valueOf(chanceString.replace("%", ""));
			} catch (NumberFormatException ex) {
				chance = 100;
			}
		}
		return chance;
	}

	private void loadSimpleDrop(ConfigurationNode node, SimpleDropEvent drop) {
		// Read drop
		boolean deny = false;
		String dropStr = node.getString("drop", "DEFAULT");
		OtherDrops.logInfo("Loading drop: "+dropStr,HIGHEST);
		if(dropStr.equals("DENY")) {
			deny = true;
			drop.setDropped(new ItemDrop(Material.AIR));
		} else drop.setDropped(DropType.parseFrom(node));
		String quantityStr = node.getString("quantity");
		if(quantityStr == null) drop.setQuantity(1);
		else drop.setQuantity(DoubleRange.parse(quantityStr));
		// Damage
		drop.setAttackerDamage(IntRange.parse(node.getString("damageattacker", "0"))); //TODO: use parseChangeFrom for this to allow %
		drop.setToolDamage(ShortRange.parse(node.getString("damagetool", "0")));
		// Spread chance
		Object spread = node.getProperty("dropspread");
		if(spread instanceof Boolean) drop.setDropSpread((Boolean) spread);
		else if(spread instanceof Number) drop.setDropSpread(((Number) spread).doubleValue());
		else drop.setDropSpread(true);
		// Replacement block
		if(deny) drop.setReplacement(new BlockTarget((Material)null)); // TODO: is this enough?  deny should also deny creature kills
		else drop.setReplacement(parseReplacement(node));
		// Random location multiplier
		String randomLoc = node.getString("randomiseloc");
		if (randomLoc != null) {
			String[] split = randomLoc.split("/");
			if (split.length == 3) {
				try {
					drop.setRandomLocMult(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]));
				} catch (Exception ex) {}
			}
		}
		// Commands, messages, sound effects
		drop.setCommands(getMaybeList(node, "commands"));
		drop.setMessages(getMaybeList(node, "message"));
		drop.setEffects(SoundEffect.parseFrom(node));
		// Events
		List<SpecialResult> dropEvents = SpecialResult.parseFrom(node);
		if(dropEvents == null) return; // We're done! Note, this means any new options must go above events!
		ListIterator<SpecialResult> iter = dropEvents.listIterator();
		while(iter.hasNext()) {
			SpecialResult event = iter.next();
			if(!event.canRunFor(drop)) iter.remove();
		}
		drop.setEvents(dropEvents);
	}

	private void loadDropGroup(ConfigurationNode node, GroupDropEvent group, Target target, Action action) {
		if(!node.getKeys().contains("drops")) {
			OtherDrops.logWarning("Empty drop group \"" + group.getName() + "\"; will have no effect!");
			return;
		}
		List<ConfigurationNode> drops = node.getNodeList("drops", null);
		for(ConfigurationNode dropNode : drops) {
			boolean isGroup = dropNode.getKeys().contains("dropgroup");
			if(isGroup) {
				OtherDrops.logWarning("Drop groups cannot be nested; skipping...");
				continue;
			}
			SimpleDropEvent drop = new SimpleDropEvent(target, action);
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

	private BlockTarget parseReplacement(ConfigurationNode node) {
		String block = node.getString("replacementblock");
		if(block == null) block = node.getString("replace");
		if(block == null) return null;
		String[] split = block.split("@");
		String name = split[0];
		String dataStr = split.length > 1 ? split[1] : "";
		Material mat = null;
		try {
			mat = Material.getMaterial(Integer.parseInt(name));
		} catch(NumberFormatException e) {
			mat = Material.getMaterial(name.toUpperCase());
		}
		if(mat == null) return null;
		if(dataStr.isEmpty()) return new BlockTarget(mat);
		Data data;
		try {
			int intData = Integer.parseInt(dataStr);
			return new BlockTarget(mat, intData);
		} catch(NumberFormatException e) {
			data = SimpleData.parse(mat, dataStr);
		}
		if(data == null) return new BlockTarget(mat);
		return new BlockTarget(mat, data);
		
	}

	public static boolean parseDropInMobArenaFrom(ConfigurationNode node) {
		return node.getBoolean("dropinmobarena", false);
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
					OtherDrops.logWarning("Invalid world " + name + "; skipping...");
					continue;
				}
				result.put(world, false);
			} else result.put(world, true);
		}
		for(String name : regionsExcept) {
			World world = Bukkit.getServer().getWorld(name);
			if(world == null) {
				OtherDrops.logWarning("Invalid world exception " + name + "; skipping...");
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
					OtherDrops.logWarning("Invalid biome " + name + "; skipping...");
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
					OtherDrops.logWarning("Invalid block face " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			} else result.put(storm, true);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	public static Map<Agent, Boolean> parseAgentFrom(ConfigurationNode node) {
		List<String> tools = OtherDropsConfig.getMaybeList(node, "tool");
		List<String> toolsExcept = OtherDropsConfig.getMaybeList(node, "toolexcept");
		Map<Agent, Boolean> toolMap = new HashMap<Agent, Boolean>();
		if(tools.isEmpty() && toolsExcept.isEmpty()) {
			toolMap.put(parseAgent("ALL"), true); // no tool defined - default to all
			return toolMap;
		}
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
			} else if (agent != null) toolMap.put(agent, flag);
		}
		for(String tool : toolsExcept) {
			Agent agent = parseAgent(tool);
			if(agent instanceof MaterialGroupAgent) {
				for(Material mat : ((MaterialGroupAgent) agent).getMaterials())
					toolMap.put(new ToolAgent(mat), false);
			} else if (agent != null) toolMap.put(agent, false);
		}
		return toolMap;
	}

	public static Agent parseAgent(String agent) {
		String[] split = agent.split("@");
		// TODO: because data = "" then data becomes 0 in toolagent rather than null - fixed in toolagent, need to check other agents
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
		if(name.startsWith("ANY") || name.equals("ALL")) return AnySubject.parseAgent(name);
		else if(name.startsWith("DAMAGE_")) return EnvironmentAgent.parse(name, data);
		else if(isCreature(name)) return CreatureSubject.parse(name, data);
		else if(name.startsWith("PROJECTILE_")) return ProjectileAgent.parse(name, data);
		else if(name.startsWith("EXPLOSION_")) return ExplosionAgent.parse(name, data);
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
		if(name.equals("PLAYER")) return PlayerSubject.parse(data);
		else if(name.equals("PLAYERGROUP")) return new GroupSubject(data);
		else if(name.startsWith("ANY") || name.equals("ALL")) return AnySubject.parseTarget(name);
		else if(isCreature(name)) return CreatureSubject.parse(name, data);
		else if(name.equalsIgnoreCase("SPECIAL_LEAFDECAY")) return BlockTarget.parse("LEAVES", data); // for compatibility
		else return BlockTarget.parse(name, data);
	}

	// TODO: put this in a better location
	public static boolean isCreature(String name) {
		if (name.startsWith("CREATURE_")) return true;
		name = name.split("@")[0];
		try {
			if (CreatureType.valueOf(name) != null) return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
		
		return false;
	}
	
	public ConfigurationNode getEventNode(SpecialResultHandler event) {
		String name = event.getName();
		if (events == null) {
			OtherDrops.logInfo("EventLoader ("+name+") failed to get config-node, events is null.",HIGH);
			return null;
		}
		ConfigurationNode node = events.getNode(name);
		if(node == null) {
			events.setProperty(name, new HashMap<String,Object>());
			node = events.getNode(name);
		}

		return node;
	}
	
	public Verbosity getVerbosity() {
		return verbosity;
	}
}