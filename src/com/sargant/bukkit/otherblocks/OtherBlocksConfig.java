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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.entity.*;

import com.gmail.zarius.common.CommonMaterial;
import com.gmail.zarius.common.CommonPlugin;

public class OtherBlocksConfig {

	public Boolean usePermissions;

	private OtherBlocks parent;

	protected Integer verbosity;
	protected Priority pri;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;
	protected List<OB_Drop> transformList;
	protected List<OBContainer_DropGroups> blockList;

	private ArrayList<String> defaultWorlds = null;
	private ArrayList<String> defaultBiomes = null;
	private ArrayList<String> defaultWeather = null;
	private ArrayList<String> defaultPermissionGroups = null;
	private ArrayList<String> defaultPermissionGroupsExcept = null;
	private String defaultTime = null;
	

	public OtherBlocksConfig(OtherBlocks instance) {
		parent = instance;
		transformList = new ArrayList<OB_Drop>();
		blockList = new ArrayList<OBContainer_DropGroups>();
	}

	// load 
	public void load() {
		Boolean firstRun = true;
		loadConfig(firstRun);
	}

	public void reload()
	{
		Boolean firstRun = false;
		loadConfig(firstRun);
		parent.setupPermissions();
		//		parent.setupPermissions(this.usepermissions);

	}

	// Short functions
	//
	void logWarning(String msg) {
		parent.log.warning("["+parent.getDescription().getName()+"] "+msg);		
	}
	void logInfo(String msg) {
		parent.log.info("["+parent.getDescription().getName()+"] "+msg);
	}

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

	public static String getDropEmbeddedQuantity(String s)  {
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


	protected static void setAttackerDamage(OB_Drop obc, String dataString) {
		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			obc.setAttackerDamage(Integer.parseInt(dataStringRangeParts[1]), Integer.parseInt(dataStringRangeParts[2]));
		} else {
			obc.setAttackerDamage(Integer.parseInt(dataString));
		}
	}

	protected static void setDataValues(OB_Drop obc, String dataString, String objectString) {

		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			// TOFIX:: check for valid numbers - or is this checked earlier?
			obc.setData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
		} else {
			obc.setData(CommonMaterial.getAnyDataShort(objectString, dataString));
		}
	}

	protected static void setDropDataValues(OB_Drop obc, String dataString, String objectString) {

		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			// TOFIX:: check for valid numbers - or is this checked earlier?
			obc.setDropData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
		} else {
			obc.setDropData(CommonMaterial.getAnyDataShort(objectString, dataString));
		}
	}

	public ArrayList<String> getArrayList(Object getVal, Boolean anyAll)
	{
		ArrayList<String> arrayList = new ArrayList<String>();

		if(getVal == null) {
			arrayList.add((String) null);
		}
		else if(getVal instanceof String) {

			String getValString = (String) getVal;

			if (anyAll) {
				if(getValString.equalsIgnoreCase("ALL") || getValString.equalsIgnoreCase("ANY")) {
					arrayList.add((String) null);
				} else {
					arrayList.add(getValString);
				}
			} else {
				arrayList.add(getValString);
			}

		} else if (getVal instanceof List<?>) {

			for(Object listPart : (List<?>) getVal) {
				arrayList.add((String) listPart);
			}

		} else {
			// cannot throw in subfunction - catch null value and throw exception in main loadconfig function
			//throw new Exception("Not a recognizable type");
			return null;
		}
		return arrayList;
	}

	// LONGER FUNCTIONS
	public void loadConfig(boolean firstRun)
	{
		String globalConfigName = ("otherblocks-globalconfig");
		File yml = new File(parent.getDataFolder(), globalConfigName+".yml");
		Configuration globalConfig = new Configuration(yml);
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 

		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				logInfo("Created an empty file " + parent.getDataFolder() +"/"+globalConfigName+", please edit it!");
				globalConfig.setProperty("otherblocks", null);
				globalConfig.save();
			} catch (IOException ex){
				logWarning(parent.getDescription().getName() + ": could not generate "+globalConfigName+". Are the file permissions OK?");
			}
		}

		// need to load the configuration for the reload command, otherwise config stays cached
		globalConfig.load();

		// Load in the values from the configuration file
		this.verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
		this.pri = CommonPlugin.getConfigPriority(globalConfig);

		List <String> keys = CommonPlugin.getConfigRootKeys(globalConfig);

		// blockto/water damage is experimental, enable only if explicitly set
		if (keys.contains("enableblockto")) {
			if (globalConfig.getString("enableblockto").equalsIgnoreCase("true")) {
				enableBlockTo = true;
				logWarning("blockto/damage_water enabled - BE CAREFUL");
			} else {
				enableBlockTo = false;
			}
		}

		// blockto/water damage is experimental, enable only if explicitly set
		if (keys.contains("usepermissions")) {
			if (globalConfig.getString("usepermissions").equalsIgnoreCase("true")) {
				this.usePermissions = true;
				parent.usePermissions = true;
			} else {
				this.usePermissions = false;
				parent.usePermissions = false;
			}
		}

		// Read the config file version
		Integer configVersion = 1;
		if (keys.contains("configversion")) {
			if (globalConfig.getString("configversion").equalsIgnoreCase("1")) {
				configVersion = 1;
			} else if (globalConfig.getString("configversion").equalsIgnoreCase("2")) {
				configVersion = 2;
			} else {
				configVersion = 2; // assume latest version
			}
		}

		transformList.clear();
		blockList.clear();

		// load the globalconfig "OtherBlocks" section
		if (configVersion == 1) {
			System.out.println("loading version 1");
			loadSpecificFile(globalConfigName);
		} else {
			System.out.println("loading version 2");
			loadSpecificFileVersion2(globalConfigName);
		}


		// scan "include-files:" for additional files to load
		if(!keys.contains("include-files"))
		{
			//TODO: make this only show on verbosity 3
			if (parent.verbosity >= 3) {
			logInfo(parent.getDescription().getName() + ": no 'include-files' key found (optional)");
			}
			return;
		}

		keys.clear();
		keys = globalConfig.getKeys("include-files");

		if(null == keys)
		{
			// TODO: make this only show on verbosity 3
			if (parent.verbosity >= 3) {
			logInfo(parent.getDescription().getName() + ": no values found in include-files tag.");
			}
			return;
		}

		// keys found, clear existing (if any) transformlist
		//transformList.clear();

		for(String s : keys) {
			//			List<Object> original_children = getConfiguration().getList("otherblocks."+s);

			//		if(original_children == null) {
			//	log.warning("Block \""+s+"\" has no children. Have you included the dash?");
			//	continue;
			
			// Reset default values
			defaultWorlds = null;
			defaultBiomes = null;
			defaultWeather = null;
			defaultPermissionGroups = null;
			defaultPermissionGroupsExcept = null;
			defaultTime = null;

			if (globalConfig.getString("include-files."+s, "true").equalsIgnoreCase("true")) {
				if (configVersion == 1) {
					loadSpecificFile(s);
				} else {
					loadSpecificFileVersion2(s);
				}
			}
		}


	}


	void loadSpecificFile(String filename) {

		filename = filename+".yml";
		File yml = new File(parent.getDataFolder(), filename);
		Configuration configFile = new Configuration(yml);

		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			logInfo("Trying to include: " + parent.getDataFolder() +"/"+filename+" but it does not exist!");
		}


		if (configFile == null) {
			return;
		}
		configFile.load(); // just in case

		List <String> keys = CommonPlugin.getConfigRootKeys(configFile);

		if(keys == null) {
			logWarning("No parent key not found.");
			return;
		}


		if(!keys.contains("otherblocks"))
		{
			logWarning("No 'otherblocks' key found.");
			return;
		}

		keys.clear();
		keys = configFile.getKeys("otherblocks");

		if(null == keys)
		{
			logInfo("No values found in config file!");
			return;
		}

		// BEGIN read default values

		List<Object> original_children = configFile.getList("defaults");

		if(original_children == null) {
			if (parent.verbosity >= 3) {
				logInfo("Defaults has no children (optional)");
			}
		} else {

			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {

					//OB_Drop bt = new OB_Drop();

					try {
						HashMap<?, ?> m = (HashMap<?, ?>) o;

						defaultWorlds = getArrayList(m.get("worlds"), true);
						if (defaultWorlds == null) defaultWorlds = getArrayList(m.get("world"), true);
						defaultBiomes = getArrayList(m.get("biomes"), true);
						if (defaultBiomes == null) defaultBiomes = getArrayList(m.get("biome"), true);
						defaultWeather = getArrayList(m.get("weather"), true);
						defaultPermissionGroups = getArrayList(m.get("permissiongroup"), true);
						defaultPermissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
						defaultTime = String.valueOf(m.get("time"));
					} catch(Throwable ex) {
					}
				}
			}
		}
		// END read default values



		for(String s : keys) {
			original_children = configFile.getList("otherblocks."+s);

			if(original_children == null) {
				logWarning("Block \""+s+"\" has no children. Have you included the dash?");
				continue;
			}

			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {

		
					OB_Drop bt = readTool(s, o, configFile);
					if (bt != null) transformList.add(bt);
				}
			}
		}
		logInfo("CONFIG: '"+filename+"' loaded.");
	}

	void loadSpecificFileVersion2(String filename) {

		// append .yml extension (cannot include this in config as fullstop is a special character, cleaner this way anyway)
		filename = filename+".yml";
		File yml = new File(parent.getDataFolder(), filename);
		Configuration configFile = new Configuration(yml);

		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			logInfo("Trying to include: " + parent.getDataFolder() +"/"+filename+" but it does not exist!");
		}


		if (configFile == null) {
			return;
		}
		configFile.load(); // just in case

		List <String> keys = CommonPlugin.getConfigRootKeys(configFile);

		if(keys == null) {
			logWarning("No parent key not found.");
			return;
		}


		if(!keys.contains("otherblocks"))
		{
			logWarning("No 'otherblocks' key found.");
			return;
		}

		keys.clear();
		keys = configFile.getKeys("otherblocks");

		if(null == keys)
		{
			logInfo("No values found in config file!");
			return;
		}

		// BEGIN read default values

		List<Object> original_children = configFile.getList("defaults");

		if(original_children == null) {
			if (parent.verbosity >= 3) {
				logInfo("Defaults has no children (optional)");
			}
		} else {

			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {

					try {
						HashMap<?, ?> m = (HashMap<?, ?>) o;

						defaultWorlds = getArrayList(m.get("worlds"), true);
						if (defaultWorlds == null) defaultWorlds = getArrayList(m.get("world"), true);
						defaultBiomes = getArrayList(m.get("biomes"), true);
						if (defaultBiomes == null) defaultBiomes = getArrayList(m.get("biome"), true);
						defaultWeather = getArrayList(m.get("weather"), true);
						defaultPermissionGroups = getArrayList(m.get("permissiongroup"), true);
						defaultPermissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
						defaultTime = String.valueOf(m.get("time"));
					} catch(Throwable ex) {
					}
				}
			}
		}
		// END read default values


		logWarning("attempting to read keys");


		for(String currentKey : keys) {
			String currentPath = "otherblocks."+currentKey;
			original_children = configFile.getList(currentPath);

			if(original_children == null) {
				logWarning("Block \""+currentKey+"\" has no children. Have you included the dash?");
				continue;
			}
			
			blockList.add(readBlock(currentPath, configFile, currentKey));
		}
		logInfo("CONFIG: "+filename+" loaded.");
	}

/*		private OBContainer_Group readGroup(String currentPath, Configuration configFile) {
			OBContainer_Group group = null;
			List<String> groupChildren = configFile.getKeys(currentPath);

			if(groupChildren == null) {
				logWarning("Group \""+currentPath+"\" has no children. Have you included the dash?");
				return null;
			}
			for(String groupChild : groupChildren) {
				currentPath = currentPath+"."+groupChild;
				group.add(readBlock(currentPath, configFile));
			}
			return group;
		}*/

		private OBContainer_DropGroups readBlock(String currentPath, Configuration configFile, String blockName) {
			OBContainer_DropGroups dropGroups = new OBContainer_DropGroups();

			List<Object> blockChildren = configFile.getList(currentPath);

			if(blockChildren == null) {
				logWarning("Block \""+currentPath+"\" has no children. Have you included the dash?");
				return null;
			}
			//for(String blockChild : blockChildren) {

			for(Object blockChild : blockChildren) {
				logWarning("inside readblock loop");
				if(blockChild instanceof HashMap<?,?>) {
					HashMap<?, ?> m = (HashMap<?, ?>) blockChild;

					if (m.get("dropgroup") != null) {
						dropGroups.list.add(readDropGroup(currentPath, configFile, blockName));
					} else {
						OB_Drop drop = readTool(blockName, blockChild, configFile);
						if (!(drop == null)) {
							OBContainer_Drops drops = new OBContainer_Drops();
							drops.list.add(drop);
							dropGroups.list.add(drops);
						}
					}
				}
			}
			return dropGroups;
		}

		private OBContainer_Drops readDropGroup(String currentPath, Configuration configFile, String blockName)
		{
			OBContainer_Drops dropGroup = new OBContainer_Drops();

			List<Object> blockChildren = configFile.getList(currentPath);

			if(blockChildren == null) {
				logWarning("Block \""+currentPath+"\" has no children. Have you included the dash?");
				return null;
			}
			//for(String blockChild : blockChildren) {

			for(Object blockChild : blockChildren) {
				if(blockChild instanceof HashMap<?,?>) {
					dropGroup.list.add(readTool(blockName, blockChild, configFile));
				} else {
					OB_Drop toolContainer = readTool(blockName, blockChild, configFile);
					dropGroup.list.add(toolContainer);
				}				
			}

			return dropGroup; 
		}
		private OB_Drop readTool(String s, Object o, Configuration configFile) {    
			OB_Drop bt = new OB_Drop();

			try {
				HashMap<?, ?> m = (HashMap<?, ?>) o;


				// Source block
				String blockString = getDataEmbeddedBlockString(s);
				String dataString = getDataEmbeddedDataString(s);

				bt.original = null;
				bt.setData(null);
				try {
					Integer block = Integer.valueOf(blockString);
					bt.original = blockString;
				} catch(NumberFormatException x) {
					if(isCreature(blockString)) {
						// Sheep can be coloured - check here later if need to add data vals to other mobs
						bt.original = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
						if(blockString.contains("SHEEP")) {
							setDataValues(bt, dataString, "WOOL");
						} else {
							setDataValues(bt, dataString, blockString);
						}
					} else if(isPlayer(s)) {
						bt.original = s;
					} else if(isPlayerGroup(s)) {
						bt.original = s;
					} else if(isLeafDecay(blockString)) {
						bt.original = blockString;
						setDataValues(bt, dataString, "LEAVES");
					} else if(isSynonymString(blockString)) {
						if(!CommonMaterial.isValidSynonym(blockString)) {
							throw new IllegalArgumentException(blockString + " is not a valid synonym");
						} else {
							bt.original = blockString;
						}
					} else {
						bt.original = Material.valueOf(blockString).toString();
						setDataValues(bt, dataString, blockString);
					}
				}

				// Tool used
				bt.tool = new ArrayList<String>();

					if(isLeafDecay(bt.original)) {
						bt.tool.add(null);
					} else if(m.get("tool") instanceof Integer) {
						Integer tool = (Integer) m.get("tool");
						bt.tool.add(tool.toString());
					} else if(m.get("tool") instanceof String) {
						String toolString = (String) m.get("tool");
						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.tool.add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.tool.add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
						    bt.tool.add(toolString);
						} else {
							bt.tool.add(Material.valueOf(toolString).toString());
						}
					} else if (m.get("tool") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("tool")) {
							String t = (String) listTool;
							if(CommonMaterial.isValidSynonym(t)) {
								bt.tool.add(t);
							} else if(isDamage(t)) {
							    bt.tool.add(t);
							//} else if(isCreature(t)) {
                            //    bt.tool.add(t);
                            } else {
								bt.tool.add(Material.valueOf(t).toString());
							}
						}

					} else {
						throw new Exception("Not a recognizable type");
					}

				// Tool EXCEPTIONS

				if (m.get("toolexcept") == null) {
					bt.toolExceptions = null;
				} else {
					bt.toolExceptions = new ArrayList<String>();
					if(isLeafDecay(bt.original)) {
						bt.toolExceptions.add(null);
					} else if(m.get("toolexcept") instanceof String) {

						String toolString = (String) m.get("toolexcept");

						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.toolExceptions.add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.toolExceptions.add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
							bt.toolExceptions.add(toolString);
						} else {
							bt.toolExceptions.add(Material.valueOf(toolString).toString());
						}

					} else if (m.get("toolexcept") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("toolexcept")) {
							String t = (String) listTool;
							if(CommonMaterial.isValidSynonym(t)) {
								bt.toolExceptions.add(t);
							} else if(isDamage(t)) {
								bt.toolExceptions.add(t);
								//} else if(isCreature(t)) {
								//    bt.tool.add(t);
							} else {
								bt.toolExceptions.add(Material.valueOf(t).toString());
							}
						}

					} else {
						throw new Exception("Toolexcept: Not a recognizable type");
					}
				}

				// Dropped item
				String fullDropString = String.valueOf(m.get("drop"));
				String dropString = getDataEmbeddedBlockString(fullDropString);
				String dropDataString = getDataEmbeddedDataString(fullDropString);

				try {
					Integer block = Integer.valueOf(fullDropString);
					bt.dropped = fullDropString;
				} catch(NumberFormatException x) {
					if(dropString.equalsIgnoreCase("DYE")) dropString = "INK_SACK";
					if(dropString.equalsIgnoreCase("NOTHING")) dropString = "AIR";

					if (dropString.startsWith("MONEY")) {
						bt.dropped = dropString;
					} else if(isCreature(dropString)) {
						bt.dropped = "CREATURE_" + CreatureType.valueOf(creatureName(dropString)).toString();
						setDropDataValues(bt, dropDataString, dropString);
					} else if(dropString.equalsIgnoreCase("CONTENTS")) {
					    bt.dropped = "CONTENTS";
					} else if(dropString.equalsIgnoreCase("DEFAULT")) {
					    bt.dropped = "DEFAULT";
					} else if(dropString.equalsIgnoreCase("DENY")) {
						bt.dropped = "DENY";
					} else if(dropString.equalsIgnoreCase("NODROP")) {
						bt.dropped = "NODROP";
					} else {
						bt.dropped = Material.valueOf(dropString).toString();
						setDropDataValues(bt, dropDataString, dropString);
					}
				}

				bt.setAttackerDamage(0);
				String attackerDamageString = String.valueOf(m.get("damageattacker"));
				if (m.get("damageattacker") != null) {
					setAttackerDamage(bt, attackerDamageString);
				}

				// Dropped color
				String dropColor = String.valueOf(m.get("color"));

				if (m.get("color") != null) {
					bt.setDropData(CommonMaterial.getAnyDataShort(bt.dropped, dropColor));
				}

				// Dropped quantity
				bt.setQuantity(Float.valueOf(1));
				if (m.get("quantity") != null) {
					try {
						Double dropQuantity = Double.valueOf(m.get("quantity").toString());
						//log.info(dropQuantity.toString());
						bt.setQuantity(dropQuantity.floatValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("quantity"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setQuantity(Float.valueOf(split[0]), Float.valueOf(split[1]));									
						} else {
							logWarning("[BLOCK: "+bt.original+"] Invalid quantity - set to 1.");
						}
					}
				}

				// Tool damage
				Integer toolDamage = Integer.class.cast(m.get("damage"));
				bt.damage = (toolDamage == null || toolDamage < 0) ? 1 : toolDamage;

				// Drop probability
				Double dropChance;
				try {
					dropChance = Double.valueOf(String.valueOf(m.get("chance")));
					bt.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
				} catch(NumberFormatException ex) {
					bt.chance = 100.0;
				}
				
				// Applicable worlds
				String getString;
				
				getString = "world";
				if (m.get(getString) == null) getString = "worlds";															
				bt.worlds = getArrayList(m.get(getString), true);
				if (bt.worlds == null) {
					if (defaultWorlds == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.worlds = defaultWorlds;
					}
				}

				// Get applicable weather conditions
				bt.weather = getArrayList(m.get("weather"), true);
				if (bt.weather == null) {
					if (defaultWeather == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.weather = defaultWeather;
					}
				}
				
				// Get applicable biome conditions
				getString = "biome";
				if (m.get(getString) == null) getString = "biomes";															
				bt.biome = getArrayList(m.get(getString), true);
				if (bt.biome == null) throw new Exception("Not a recognizable type");
				if (bt.biome == null) {
					if (defaultBiomes == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.biome = defaultBiomes;
					}
				}

				// Get event conditions
				bt.event = getArrayList(m.get("event"), true);
				if (bt.event == null) throw new Exception("Not a recognizable type");

				// Message
				// Applicable messages
				getString = "message";
				if (m.get(getString) == null) getString = "messages";															
				bt.messages = getArrayList(m.get(getString), false);
				if (bt.messages == null) throw new Exception("Not a recognizable type");

				// Get the time string
				String timeString = String.valueOf(m.get("time"));
				if(m.get("time") == null) {
					bt.time = defaultTime;
				} else {
					bt.time = timeString;
				}

				// Get the exclusive string
				String exlusiveString = String.valueOf(m.get("exclusive"));
				if(m.get("exclusive") == null) {
					bt.exclusive = null;
				} else {
					bt.exclusive = exlusiveString;
				}

				// Get permission groups
				bt.permissionGroups = getArrayList(m.get("permissiongroup"), true);
				if (bt.permissionGroups == null) throw new Exception("Not a recognizable type");
				if (bt.permissionGroups == null) {
					if (defaultPermissionGroups == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.permissionGroups = defaultPermissionGroups;
					}
				}
				
				// Get permission groups
				bt.permissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
				if (bt.permissionGroupsExcept == null) throw new Exception("Not a recognizable type");
				if (bt.permissionGroupsExcept == null) {
					if (defaultPermissionGroupsExcept == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.permissionGroupsExcept = defaultPermissionGroupsExcept;
					}
				}

				
				String heightString = String.valueOf(m.get("height"));
				if(m.get("height") == null) {
					bt.height = null;
				} else {
					bt.height = heightString;
				}


			} catch(Throwable ex) {
				if(verbosity > 1) {
					logWarning("Error while processing block " + s + ": " + ex.getMessage());
				}

				ex.printStackTrace();
				return null;
			}

			if(verbosity > 1) {
				logInfo("BLOCK: " +
						(bt.tool.contains(null) ? "ALL TOOLS" : (bt.tool.size() == 1 ? bt.tool.get(0).toString() : bt.tool.toString())) + " + " +
						creatureName(bt.original) + bt.getData() + " now drops " +
						(bt.getQuantityRange() + "x ") + 
						creatureName(bt.dropped) + "@" + bt.getDropDataRange() +
						(bt.chance < 100 ? " with " + bt.chance.toString() + "% chance" : ""));
			}

			return bt;

		}
	}