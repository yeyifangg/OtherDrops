// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
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
import java.io.FileNotFoundException;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventPriority;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CommonPlugin;
import com.gmail.zariust.common.MaterialGroup;
import static com.gmail.zariust.common.CommonPlugin.*;

import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.common.Verbosity;
import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SimpleData;
import com.gmail.zariust.otherdrops.event.*;
import com.gmail.zariust.otherdrops.drop.CreatureDrop;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.ItemDrop;
import com.gmail.zariust.otherdrops.options.*;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.parameters.actions.PotionAction;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.special.SpecialResultHandler;
import com.gmail.zariust.otherdrops.special.SpecialResultLoader;
import com.gmail.zariust.otherdrops.subject.*;

public class OtherDropsConfig {

	public boolean usePermissions;

	private OtherDrops parent;

	public boolean dropForBlocks; // this is set to true if config for blocks found
	public boolean dropForCreatures; // this is set to true if config for creatures found
	public boolean dropForExplosions; // this is set to true if config for explosions found

	public boolean customDropsForExplosions;

	protected static Verbosity verbosity = Verbosity.NORMAL;
	public static EventPriority priority = EventPriority.HIGH;

	public boolean profiling;
	
	public boolean defaultDropSpread; // determines if dropspread defaults to true or false
	public boolean enableBlockTo;
	protected boolean disableEntityDrops;
	public static boolean disableXpOnNonDefault; // if drops are configured for mobs - disable the xp unless there is a default drop
	public static int moneyPrecision;	

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
	private List<Action> defaultAction;
	
	// A place for special events to stash options
	private ConfigurationNode events;


	public OtherDropsConfig(OtherDrops instance) {
		parent = instance;
		blocksHash = new DropsMap();

		dropForBlocks = false;
		dropForCreatures = false;
		defaultDropSpread = true;
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
		try {
			loadConfig();
		} catch(ScannerException e) {
			e.printStackTrace();
			Log.logWarning("There was a syntax in your config file which has forced OtherDrops to abort loading!");
			Log.logWarning("The error was:\n" + e.toString());
			Log.logInfo("You can fix the error and reload with /odr.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.logWarning("Config file not found!");
			Log.logWarning("The error was:\n" + e.toString());
			Log.logInfo("You can fix the error and reload with /odr.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.logWarning("There was an IO error which has forced OtherDrops to abort loading!");
			Log.logWarning("The error was:\n" + e.toString());
			Log.logInfo("You can fix the error and reload with /odr.");
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.logWarning("Config is invalid!");
			Log.logWarning("The error was:\n" + e.toString());
			Log.logInfo("You can fix the error and reload with /odr.");
		}
		parent.setupPermissions(usePermissions);
	}
	
	public void loadConfig() throws FileNotFoundException, IOException, InvalidConfigurationException
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
		YamlConfiguration globalConfig = YamlConfiguration.loadConfiguration(global);
		
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!global.exists()) {
			try {
				global.createNewFile();
				Log.logInfo("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
				globalConfig.set("verbosity", "normal");
				globalConfig.set("priority", "high");
				globalConfig.set("usepermissions", true);
				globalConfig.save(global);
			} catch (IOException ex){
				Log.logWarning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
			}
		}

		// Load in the values from the configuration file
		globalConfig.load(global);
		String configKeys = globalConfig.getKeys(false).toString();
		
		verbosity = getConfigVerbosity(globalConfig);
		priority = getConfigPriority(globalConfig);
		enableBlockTo = globalConfig.getBoolean("enableblockto", false);
		usePermissions = globalConfig.getBoolean("useyetipermissions", false);
		moneyPrecision = globalConfig.getInt("money-precision", 2);
		customDropsForExplosions = globalConfig.getBoolean("customdropsforexplosions", false);
		defaultDropSpread = globalConfig.getBoolean("default_dropspread", true);
		disableXpOnNonDefault = globalConfig.getBoolean("disable_xp_on_non_default", true);

		String mainDropsName = globalConfig.getString("rootconfig", "otherdrops-drops.yml");
		if (!(new File(parent.getDataFolder(), mainDropsName).exists())
			&& new File(parent.getDataFolder(), "otherblocks-globalconfig.yml").exists())
			mainDropsName = "otherblocks-globalconfig.yml"; // Compatibility with old filename

		events = new ConfigurationNode(globalConfig.getConfigurationSection("events"));
		if(events == null) {
			globalConfig.set("events", new HashMap<String,Object>());
			events = new ConfigurationNode(new HashMap<String,Object>());
			if(events == null) Log.logWarning("EVENTS ARE NULL");
			else Log.logInfo("Events node created.", NORMAL);
		}
		
		// Warn if DAMAGE_WATER is enabled
		if(enableBlockTo) Log.logWarning("blockto/damage_water enabled - BE CAREFUL");

		try {
			SpecialResultLoader.loadEvents();
		} catch (Exception except) {
			Log.logWarning("Event files failed to load - this shouldn't happen, please inform developer.");
			if(verbosity.exceeds(HIGHEST)) except.printStackTrace();
		}

		Log.logInfo("Loaded global config ("+global+"), keys found: "+configKeys + " (verbosity="+verbosity+")", Verbosity.HIGH);

		loadDropsFile(mainDropsName);
		blocksHash.applySorting();
	}

	private void loadDropsFile(String filename) {
		// Check for infinite include loops
		if(loadedDropFiles.contains(filename)) {
			Log.logWarning("Infinite include loop detected at " + filename);
			return;
		} else loadedDropFiles.add(filename);
		
		Log.logInfo("Loading file: "+filename,NORMAL);
		
		File yml = new File(parent.getDataFolder(), filename);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(yml);
		
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				Log.logInfo("Created an empty file " + parent.getDataFolder() +"/"+filename+", please edit it!");
				config.set("otherdrops", null);
				config.set("include-files", null);
				config.set("defaults", null);
				config.set("aliases", null);
				config.set("configversion", 3);
				config.save(yml);
			} catch (IOException ex){
				Log.logWarning(parent.getDescription().getName() + ": could not generate "+filename+". Are the file permissions OK?");
			}
			// Nothing to load in this case, so exit now
			return;
		}
		
		try {
			config.load(yml);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Warn if wrong version
		int configVersion = config.getInt("configversion", 3);
		if(configVersion < 3)
			Log.logWarning("config file appears to be in older format; some things may not work");
		else if(configVersion > 3)
			Log.logWarning("config file appears to be in newer format; some things may not work");
		
		// Load defaults; each of these functions returns null if the value isn't found
		ConfigurationNode defaults = new ConfigurationNode(config.getConfigurationSection("defaults"));

		// Check for null - it's possible that the defaults key doesn't exist or is empty
		defaultAction = Collections.singletonList(Action.BREAK);
		if (defaults != null) {
			Log.logInfo("Loading defaults...",HIGH);
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
			defaultAction = Action.parseFrom(defaults, defaultAction);
		} else Log.logInfo("No defaults set.",HIGHEST);
			
		// Load the drops
		ConfigurationSection node = config.getConfigurationSection("otherdrops");
		Set<String> blocks = null;
		if (node != null) blocks = node.getKeys(false);

		if(node == null) { // Compatibility
			node = config.getConfigurationSection("otherblocks");
			if (node != null) blocks = node.getKeys(false);
		}
		if (node != null) {
		    for(Object blockNameObj : blocks.toArray()) {
		    	String blockName = "";
		    	blockName = blockNameObj.toString();
	            
	            if (blockNameObj instanceof Integer) {
		            Log.logWarning("Integer target: "+blockName+" (cannot process - please enclose in quotation marks eg. \""+blockName+"\")");
		            continue;
	            }
		    	
	            // convert spaces and dashes to underscore before parsing to allow more flexible matching
		        Target target = parseTarget(blockName.replaceAll("[ -]", "_")); 
		        if(target == null) {
		            Log.logWarning("Unrecognized target (skipping): " + blockName, verbosity.NORMAL);
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

		      //  List<Map<?, ?>> blockNode = node.getMapList(blockName);//ConfigurationSection("GRASS.drop");
			//	if (blockNode == null) OtherDrops.logInfo("Blocknode is null!!");

				List<ConfigurationNode> drops = ConfigurationNode.parse(node.getMapList(blockName));
				loadBlockDrops(drops, blockName, target);
				
				// Future modulized parameters parsing
/*				for (Map<String, Object> drop : blockNode) {
					for (String parameter : drop.keySet()) {
						String parameterKey = parameter.split(".")[0];
						Parameters.parse(parameterKey);
					}
				}
				*/
			//	OtherDrops.logInfo("Loading config... blocknode:"+blockNode.toString() +" for blockname: "+originalBlockName);
				//Set<String> drops = null;
			//    if (blockNode != null) drops = blockNode.getKeys(false);
		    //    loadBlockDrops(blockNode, blockName, target, node);
		    }
		}
		
		// Load the include files
		List<String> includeFiles = config.getStringList("include-files");
		for(String include : includeFiles) loadDropsFile(include);
	}

	private void loadBlockDrops(List<ConfigurationNode> drops, String blockName, Target target) {
		for(ConfigurationNode dropNode : drops) {
			boolean isGroup = dropNode.getKeys().contains("dropgroup");
			List<Action> actions = new ArrayList<Action>();
			List<Action> leafdecayAction = new ArrayList<Action>();
			leafdecayAction.add(Action.LEAF_DECAY);
			if (blockName.equalsIgnoreCase("SPECIAL_LEAFDECAY")) {
			actions = Action.parseFrom(dropNode, leafdecayAction);
			} else {
			actions = Action.parseFrom(dropNode, defaultAction);
			}

			if(actions.isEmpty()) {
			// FIXME: Find a way to say which action was invalid
			Log.logWarning("No recognized action for block " + blockName + "; skipping (known actions: "+Action.getValidActions().toString()+")",NORMAL);
			continue;
			}
			for(Action action : actions) {
				// TODO: This reparses the same drop once for each listed action; a way that involves parsing only once? Would require having the drop class implement clone().
				CustomDrop drop = loadDrop(dropNode, target, action, isGroup);
				if (drop.getTool() == null || drop.getTool().isEmpty()) {
					// FIXME: Should find a way to report the actual invalid tool as well
					// FIXME: Also should find a way to report when some tools are valid and some are not
					Log.logWarning("Unrecognized tool for block " + blockName + "; skipping.",NORMAL);
					continue;
				}
				blocksHash.addDrop(drop);
			}
		}
	}

	private CustomDrop loadDrop(ConfigurationNode dropNode, Target target, Action action, boolean isGroup) {
		CustomDrop drop = isGroup ? new GroupDropEvent(target, action) : new SimpleDrop(target, action);
		loadConditions(dropNode, drop);
		if(isGroup) loadDropGroup(dropNode,(GroupDropEvent) drop, target, action);
		else loadSimpleDrop(dropNode, (SimpleDrop) drop);
		return drop;
	}

	private void loadConditions(ConfigurationNode node, CustomDrop drop) {
		drop.addActions(MessageAction.parse(node));
		drop.addActions(PotionAction.parse(node));
		
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
		drop.setFlags(Flag.parseFrom(node));
		
		// Read chance, delay, etc
		drop.setChance(parseChanceFrom(node, "chance"));
		Object exclusive = node.get("exclusive");
		if(exclusive != null) drop.setExclusiveKey(exclusive.toString());
		drop.setDelay(IntRange.parse(node.getString("delay", "0")));
	}

	public static double parseChanceFrom(ConfigurationNode node, String key) {
		String chanceString = node.getString(key, null);
		double chance = 100;
		if (chanceString == null) {
			chance = 100;
		} else {
			try {
				chance = Double.parseDouble(chanceString.replaceAll("%$", ""));
			} catch (NumberFormatException ex) {
				chance = 100;
			}
		}
		return chance;
	}
	
	private Location parseLocationFrom(ConfigurationNode node, String type, double d, double defY, double e) {
		String loc = getStringFrom(node, "loc-" + type, type + "loc");
		if(loc == null) return new Location(null,d,defY,e);
		double x = 0, y = 0, z = 0;
		String[] split = loc.split("/");
		if (split.length == 3) {
			try {
				x = Double.parseDouble(split[0]);
				y = Double.parseDouble(split[1]);
				z = Double.parseDouble(split[2]);
			} catch (NumberFormatException ex) {
				x = y = z = 0;
			}
		}
		return new Location(null,x,y,z);
	}

	private void loadSimpleDrop(ConfigurationNode node, SimpleDrop drop) {
		// Read drop
		boolean deny = false;
		String dropStr = node.getString("drop", "UNSPECIFIED"); // default value should be NOTHING (DEFAULT will break some configs) FIXME: it should really be a third option - NOTAPPLICABLE, ie. doesn't change the drop
		dropStr = dropStr.replaceAll("[ -]", "_");
		if(dropStr.equals("DENY")) {
			drop.setDenied(true);
//			deny = true; // set to DENY (used later to set replacement block to null)
//			drop.setDropped(new ItemDrop(Material.AIR)); // set the drop to NOTHING
		} else drop.setDropped(DropType.parseFrom(node));
		if (drop.getDropped() != null) Log.logInfo("Loading drop: " + drop.getAction() + " with " + drop.getTool() + " on " + drop.getTarget() + " -> " + drop.getDropped().toString(),HIGHEST);
		else Log.logInfo("Loading drop (null: failed or default drop): " + drop.getAction() + " with " + drop.getTool() + " on " + drop.getTarget() + " -> \'" + dropStr+"\"",HIGHEST);
			
		String quantityStr = node.getString("quantity");
		if(quantityStr == null) drop.setQuantity(1);
		else drop.setQuantity(DoubleRange.parse(quantityStr));
		// Damage
		drop.setAttackerDamage(IntRange.parse(node.getString("damageattacker", "0")));
		drop.setToolDamage(ToolDamage.parseFrom(node));
		// Spread chance
		drop.setDropSpread(node, "dropspread", defaultDropSpread);
		// Replacement block
		drop.setReplacement(parseReplacement(node));
		// Random location multiplier
		drop.setRandomLocMult(parseLocationFrom(node, "randomise", 0, 0, 0));
		// Location offset
		if(drop.getDropped() instanceof CreatureDrop && drop.getTarget() instanceof BlockTarget)
			// Drop creature in the centre of the block, not on the corner
			drop.setLocationOffset(parseLocationFrom(node, "offset", 0.5, 1, 0.5));
		else drop.setLocationOffset(parseLocationFrom(node, "offset", 0, 0, 0));
		// Commands, messages, sound effects
		drop.setCommands(getMaybeList(node, "command", "commands"));
		drop.setMessages(getMaybeList(node, "message", "messages"));
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
			Log.logWarning("Empty drop group \"" + group.getName() + "\"; will have no effect!");
			return;
		}
		Log.logInfo("Loading drop group: " + group.getAction() + " with " + group.getTool() + " on " + group.getTarget() + " -> " + group.getName(),HIGHEST);
		group.setMessages(getMaybeList(node, "message", "messages"));

		List<ConfigurationNode> drops = node.getNodeList("drops", null);
		for(ConfigurationNode dropNode : drops) {
		boolean isGroup = dropNode.getKeys().contains("dropgroup");
		CustomDrop drop = loadDrop(dropNode, target, action, isGroup);
		group.add(drop);
		}
		group.sort();
	}
	
	public static List<String> getMaybeList(ConfigurationNode node, String... keys) {
		if(node == null) return new ArrayList<String>();
		Object prop = null;
		String key = null;
		for (int i = 0; i < keys.length; i++) {
			key = keys[i];
			prop = node.get(key);
			if(prop != null) break;
		}
		List<String> list;
		if(prop == null) return new ArrayList<String>();
		else if(prop instanceof List) list = node.getStringList(key);
		else list = Collections.singletonList(prop.toString());
		return list;
	}
	
	public static String getStringFrom(ConfigurationNode node, String... keys) {
		String prop = null;
		for(int i = 0; i < keys.length; i++) {
			prop = node.getString(keys[i]);
			if(prop != null) break;
		}
		return prop;
	}

	private BlockTarget parseReplacement(ConfigurationNode node) {
		String blockName = getStringFrom(node, "replacementblock", "replaceblock", "replace");
		if(blockName == null) return null;
		String[] split = blockName.split("@");
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
		Data data = null;
		try {
			int intData = Integer.parseInt(dataStr);
			return new BlockTarget(mat, intData);
		} catch(NumberFormatException e) {
			try {
				data = SimpleData.parse(mat, dataStr);
			} catch(IllegalArgumentException ex) {
				Log.logWarning(ex.getMessage());
				return null;
			}
		}
		if(data == null) return new BlockTarget(mat);
		return new BlockTarget(mat, data);
		
	}

	private Map<World, Boolean> parseWorldsFrom(ConfigurationNode node, Map<World, Boolean> def) {
		List<String> worlds = getMaybeList(node, "world", "worlds");
		List<String> worldsExcept = getMaybeList(node, "worldexcept", "worldsexcept");
		if(worlds.isEmpty() && worldsExcept.isEmpty()) return def;
		Map<World, Boolean> result = new HashMap<World,Boolean>();
		result.put(null, containsAll(worlds));
		for(String name : worlds) {
			World world = Bukkit.getServer().getWorld(name);
			if(world == null && name.startsWith("-")) {
				result.put(null, true);
				world = Bukkit.getServer().getWorld(name.substring(1));
				if(world == null) {
					Log.logWarning("Invalid world " + name + "; skipping...");
					continue;
				}
				result.put(world, false);
			} else if (world == null) {
				if (name.equalsIgnoreCase("ALL") || name.equalsIgnoreCase("ANY")) {
					result.put(null, true);
				} else {
					Log.logWarning("Invalid world " + name + "; skipping...");
					continue;
				}
			} else result.put(world, true);
		}
		for(String name : worldsExcept) {
			World world = Bukkit.getServer().getWorld(name);
			if(world == null) {
				Log.logWarning("Invalid world exception " + name + "; skipping...");
				continue;
			}
			result.put(null, true);
			result.put(world, false);
		}
		return result;
	}

	// TODO: refactor parseWorldsFrom, Regions & Biomes as they are all very similar - (beware - fragile, breaks easy)
	private Map<String, Boolean> parseRegionsFrom(ConfigurationNode node, Map<String, Boolean> def) {
		List<String> regions = getMaybeList(node, "region", "regions");
		List<String> regionsExcept = getMaybeList(node, "regionexcept", "regionsexcept");
		if(regions.isEmpty() && regionsExcept.isEmpty()) return def;
		Map<String, Boolean> result = new HashMap<String,Boolean>();
		for(String name : regions) {
			if(name.startsWith("-")) {
				result.put(name, false);  // deliberately including the "-" sign
			} else result.put(name, true);
		}
		for(String name : regionsExcept) {
			result.put(name, false);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	private Map<Biome, Boolean> parseBiomesFrom(ConfigurationNode node, Map<Biome, Boolean> def) {
		List<String> biomes = getMaybeList(node, "biome", "biomes");
		if(biomes.isEmpty()) return def;
		HashMap<Biome, Boolean> result = new HashMap<Biome,Boolean>();
		result.put(null, containsAll(biomes));
		for(String name : biomes) {
			Biome biome = enumValue(Biome.class, name);
			if(biome != null) result.put(biome, true);
			else if(name.startsWith("-")) {
				result.put(null, true);
				biome = enumValue(Biome.class, name.substring(1));
				if(biome == null) {
					Log.logWarning("Invalid biome " + name + "; skipping...");
					continue;
				}
				result.put(biome, false);
			}
		}
		return result;
	}

	private Map<String, Boolean> parseGroupsFrom(ConfigurationNode node, Map<String, Boolean> def) {
		List<String> groups = getMaybeList(node, "permissiongroup", "permissiongroups");
		List<String> groupsExcept = getMaybeList(node, "permissiongroupexcept", "permissiongroupsexcept");
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
		List<String> permissions = getMaybeList(node, "permission", "permissions");
		List<String> permissionsExcept = getMaybeList(node, "permissionexcept", "permissionsexcept");
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

	private Map<BlockFace, Boolean> parseFacesFrom(ConfigurationNode node) {
		List<String> faces = getMaybeList(node, "face", "faces");
		if(faces.isEmpty()) return null;
		HashMap<BlockFace, Boolean> result = new HashMap<BlockFace,Boolean>();
		result.put(null, containsAll(faces));
		for(String name : faces) {
			BlockFace storm = enumValue(BlockFace.class, name);
			if(storm == null && name.startsWith("-")) {
				result.put(null, true);
				storm = enumValue(BlockFace.class, name.substring(1));
				if(storm == null) {
					Log.logWarning("Invalid block face " + name + "; skipping...");
					continue;
				}
				result.put(storm, false);
			} else result.put(storm, true);
		}
		if(result.isEmpty()) return null;
		return result;
	}

	public static boolean containsAll(List<String> list) {
		for(String str : list) {
			if(str.equalsIgnoreCase("ALL") || str.equalsIgnoreCase("ANY")) return true;
		}
		return false;
	}

	public static Map<Agent, Boolean> parseAgentFrom(ConfigurationNode node) {
		List<String> tools = OtherDropsConfig.getMaybeList(node, "agent", "agents", "tool", "tools");
		List<String> toolsExcept = OtherDropsConfig.getMaybeList(node, "agentexcept", "agentsexcept", "toolexcept", "toolsexcept");
		Map<Agent, Boolean> toolMap = new HashMap<Agent, Boolean>();
		if(tools.isEmpty()) {
			toolMap.put(parseAgent("ALL"), true); // no tool defined - default to all
		} else for(String tool : tools) {
			Agent agent = null;
			boolean flag = true;
			if(tool.startsWith("-")) {
				agent = parseAgent(tool.substring(1));
				flag = false;
			} else agent = parseAgent(tool);
			if(agent != null) toolMap.put(agent, flag);
		}
		for(String tool : toolsExcept) {
			Agent agent = parseAgent(tool);
			if(agent != null) toolMap.put(agent, false);
		}
		return toolMap;
	}

	public static Agent parseAgent(String agent) {
		String[] split = agent.split("@");
		// TODO: because data = "" then data becomes 0 in toolagent rather than null - fixed in toolagent, need to check other agents
		String name = split[0].toUpperCase(), data = "", enchantment = "";
		if(split.length > 1) {
			data = split[1];
			String[] split2 = data.split("!");
			data = split2[0];
			if (split2.length > 1) enchantment = split2[1];
		}
		// Agent can be one of the following
		// - A tool; ie, a Material constant
		// - One of the Material synonyms NOTHING and DYE
		// - A MaterialGroup constant
		// - One of the special wildcards ANY, ANY_CREATURE, ANY_DAMAGE
		// - A DamageCause constant prefixed by DAMAGE_
		//   - DAMAGE_FIRE_TICK and DAMAGE_CUSTOM are valid but not allowed
		//   - DAMAGE_WATER is invalid but allowed, and stored as CUSTOM
		// - A EntityType constant prefixed by CREATURE_
		// - A projectile; ie a Material constant prefixed by PROJECTILE_
		if(MaterialGroup.isValid(name) || name.startsWith("ANY") || name.equals("ALL")) return AnySubject.parseAgent(name);
		else if(name.equals("PLAYER")) return PlayerSubject.parse(data);
		else if(name.equals("PLAYERGROUP")) return new GroupSubject(data);
		else if(name.startsWith("DAMAGE_")) return EnvironmentAgent.parse(name, data);
		else {
			LivingSubject creatureSubject = CreatureSubject.parse(name, data);

			if (creatureSubject != null) return creatureSubject;
			else if(name.startsWith("PROJECTILE")) return ProjectileAgent.parse(name, data);
			else if(name.startsWith("EXPLOSION")) return ExplosionAgent.parse(name, data);
			else return ToolAgent.parse(name, data, enchantment);

		}
	}

	public static Target parseTarget(String blockName) {
		String[] split = blockName.split("@");
		String name = split[0].toUpperCase(), data = "";
		if(split.length > 1) data = split[1];
		// Target name is one of the following:
		// - A Material constant that is a block, painting, or vehicle
		// - A EntityType constant prefixed by CREATURE_
		// - An integer representing a Material
		// - One of the keywords PLAYER or PLAYERGROUP
		// - Vehicle starting with VEHICLE (note: BOAT, MINECART, etc 
		            // can only be vehicles in a target so process accordingly)
		// - A MaterialGroup constant containing blocks
		if(name.equals("PLAYER")) return PlayerSubject.parse(data);
		else if(name.equals("PLAYERGROUP")) return new GroupSubject(data);
		else if(name.startsWith("ANY") || name.equals("ALL")) return AnySubject.parseTarget(name);
		else if(name.startsWith("VEHICLE") || name.matches("BOAT|MINECART")) 
			return VehicleTarget.parse(Material.getMaterial(name.replaceAll("VEHICLE_", "")), data);
		else {
			LivingSubject creatureSubject = CreatureSubject.parse(name, data);

			if (creatureSubject != null) return creatureSubject;
			else if(name.equalsIgnoreCase("SPECIAL_LEAFDECAY"))	return BlockTarget.parse("LEAVES", data); // for compatibility
			else return BlockTarget.parse(name, data);
			
		}
	}

	
	public ConfigurationNode getEventNode(SpecialResultHandler event) {
		String name = event.getName();
		if (events == null) {
			Log.logInfo("EventLoader ("+name+") failed to get config-node, events is null.",HIGH);
			return null;
		}
		ConfigurationNode node = events.getConfigurationNode(name);
		if(node == null) {
			events.set(name, new HashMap<String,Object>());
			node = events.getConfigurationNode(name);
		}

		return node;
	}
	
	public static Verbosity getVerbosity() {
		return verbosity;
	}
	
	public static EventPriority getPriority() {
		return priority;
	}

	private void setPri(EventPriority pri) {
		this.priority = pri;
	}

	public static void setVerbosity(Verbosity verbosity) {
		OtherDropsConfig.verbosity = verbosity;
	}
}