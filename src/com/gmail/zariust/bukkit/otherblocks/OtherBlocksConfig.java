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
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.entity.*;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.CommonPlugin;
import com.gmail.zariust.bukkit.otherblocks.drops.CustomDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.DropGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.DropsList;

public class OtherBlocksConfig {

	public Boolean usePermissions;

	private OtherBlocks parent;

	public static Boolean dropForBlocks; // this is set to true if config for blocks found
	public static Boolean dropForCreatures; // this is set to true if config for creatures found
	
	static protected Integer verbosity;
	static protected Priority pri;

	public static boolean profiling;

	public static boolean runCommandsSuppressMessage; // if true: "runcommands" responses go to the console rather than the player
	
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;
	protected static HashMap<String, DropsList> blocksHash;

	private ArrayList<String> defaultWorlds = null;
	private ArrayList<String> defaultBiomes = null;
	private ArrayList<String> defaultWeather = null;
	private ArrayList<String> defaultPermissionGroups = null;
	private ArrayList<String> defaultPermissionGroupsExcept = null;
	private ArrayList<String> defaultPermissions = null;
	private ArrayList<String> defaultPermissionsExcept = null;
	private String defaultTime = null;
	

	public OtherBlocksConfig(OtherBlocks instance) {
		parent = instance;
		blocksHash = new HashMap<String, DropsList>();

		dropForBlocks = false;
		dropForCreatures = false;
		
		runCommandsSuppressMessage = true;
		
		verbosity = 2;
		pri = Priority.Lowest;
	}

	// load 
	public void load() {
		loadConfig(true);
		parent.setupPermissions();
	}

	public void reload()
	{
		loadConfig(false);
		parent.setupPermissions();
		//		parent.setupPermissions(this.usepermissions);

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


	protected static void setAttackerDamage(CustomDrop obc, String dataString) {
		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			obc.setAttackerDamage(Integer.parseInt(dataStringRangeParts[1]), Integer.parseInt(dataStringRangeParts[2]));
		} else {
			obc.setAttackerDamage(Integer.parseInt(dataString));
		}
	}

	protected static void setDataValues(CustomDrop obc, String dataString, String objectString) {

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

	protected static void setDropDataValues(CustomDrop obc, String dataString, String objectString) {

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

	public ArrayList<String> getArrayList(Object getVal, Boolean anyAll) throws Exception
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

		} else { // not a string or a list - throw exception
				  // TODO: what does this return if null value?	 can we still return a val after throw exception?
				  // cannot throw in subfunction - catch null value and throw exception in main loadconfig function
			throw new Exception("Not a recognizable type");
		}
		return arrayList;
	}

	// LONGER FUNCTIONS
	public void loadConfig(boolean firstRun)
	{
		blocksHash.clear(); // clear here to avoid issues on /obr reloading
		dropForBlocks = false; // reset variable before reading config
		dropForCreatures = false; // reset variable before reading config
		
		String globalConfigName = ("otherblocks-globalconfig");
		File yml = new File(parent.getDataFolder(), globalConfigName+".yml");
		Configuration globalConfig = new Configuration(yml);
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 

		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				OtherBlocks.logInfo("Created an empty file " + parent.getDataFolder() +"/"+globalConfigName+", please edit it!");
				globalConfig.setProperty("otherblocks", null);
				globalConfig.save();
			} catch (IOException ex){
				OtherBlocks.logWarning(parent.getDescription().getName() + ": could not generate "+globalConfigName+". Are the file permissions OK?");
			}
		}

		// need to load the configuration for the reload command, otherwise config stays cached
		globalConfig.load();

		// Load in the values from the configuration file
		OtherBlocksConfig.verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
		OtherBlocksConfig.pri = CommonPlugin.getConfigPriority(globalConfig);

		List <String> keys = CommonPlugin.getConfigRootKeys(globalConfig);

		// blockto/water damage is experimental, enable only if explicitly set
		if (keys.contains("enableblockto")) {
			if (globalConfig.getString("enableblockto").equalsIgnoreCase("true")) {
				enableBlockTo = true;
				OtherBlocks.logWarning("blockto/damage_water enabled - BE CAREFUL");
			} else {
				enableBlockTo = false;
			}
		}

		// use permissions - NOT NEEDED - permissions enabled by default and new "canPlayerBuild" function protects regions/build rights
		/*if (keys.contains("usepermissions")) {
			if (globalConfig.getString("usepermissions").equalsIgnoreCase("true")) {
				this.usePermissions = true;
				parent.usePermissions = true;
			} else {
				this.usePermissions = false;
				parent.usePermissions = false;
			}
		}*/
		this.usePermissions = true;
		parent.usePermissions = true;

		// Read the config file version
		Integer configVersion = 2;
		if (keys.contains("configversion")) {
			if (globalConfig.getString("configversion").equalsIgnoreCase("1")) {
				configVersion = 1;
			} else if (globalConfig.getString("configversion").equalsIgnoreCase("2")) {
				configVersion = 2;
			} else {
				configVersion = 2; // assume latest version
			}
		}


		// load the globalconfig "OtherBlocks" section
		// only really one version at the moment - need to refactor the config to make this more clear
/*		if (configVersion == 1) {
			System.out.println("loading version 1");
		} else {
			System.out.println("loading version 2");
		}*/
		loadSpecificFileVersion(globalConfigName, configVersion);


		// scan "include-files:" for additional files to load
		if(!keys.contains("include-files"))
		{
			OtherBlocks.logInfo(parent.getDescription().getName() + ": no 'include-files' key found (optional)", 3);
			return;
		}

		keys.clear();
		keys = globalConfig.getKeys("include-files");

		if(null == keys)
		{
			OtherBlocks.logInfo(parent.getDescription().getName() + ": no values found in include-files tag.", 3);
			return;
		}

		// keys found, clear existing (if any) transformlist
		// TODO: move clear to here rather than top? in case of config file failure?
		
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
				loadSpecificFileVersion(s, configVersion);
			}
		}


	}

	void loadSpecificFileVersion(String filename, Integer version) {

		// append .yml extension (cannot include this in config as fullstop is a special character, cleaner this way anyway)
		filename = filename+".yml";
		File yml = new File(parent.getDataFolder(), filename);
		Configuration configFile = new Configuration(yml);

		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			OtherBlocks.logInfo("Trying to include: " + parent.getDataFolder() +"/"+filename+" but it does not exist!");
			return;
		}
		
		configFile.load(); // just in case

		List <String> keys = CommonPlugin.getConfigRootKeys(configFile);

		if(keys == null) {
			OtherBlocks.logWarning("No parent key found.");
			return;
		}


		if(!keys.contains("otherblocks"))
		{
			OtherBlocks.logWarning("No 'otherblocks' key found.", 2);
			return;
		}

		keys.clear();
		keys = configFile.getKeys("otherblocks");

		if(null == keys)
		{
			OtherBlocks.logInfo("No values found in config file!");
			return;
		}

		// BEGIN read default values

		List<Object> original_children = configFile.getList("defaults");

		if(original_children == null) {
			OtherBlocks.logInfo("Defaults has no children (optional)", 3);
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
						defaultPermissions = getArrayList(m.get("permissions"), true);
						defaultPermissionsExcept = getArrayList(m.get("permissionsexcept"), true);
						defaultTime = String.valueOf(m.get("time"));
					} catch(Throwable ex) {
					}
				}
			}
		}
		// END read default values


		OtherBlocks.logInfo("CONFIG: loading keys for file: "+filename,3);


		for(Object currentKeyObj : keys) {
			// Each currentKeyObj is one block (SAND, GRASS, etc)

			// Trying to allow integer block values rather than needing eg. "4"
			// This bit doesn't work since the Bukkit getKeys function is a 
			// list of Strings and fails with Integers on the "for(Object currentKeyObj : keys)" line
			String currentKey = "";
			if (currentKeyObj instanceof String)
			{
				currentKey = (String) currentKeyObj;
			} else if (currentKeyObj instanceof Integer) {
				currentKey = currentKeyObj.toString();
			} else {
				OtherBlocks.logWarning("Block \""+currentKeyObj.toString()+"\" is not a string or an integer, skipping.");
				continue;
			}
			
			// Grab the children of the current block (generally lists)
			String currentPath = "otherblocks."+currentKey;
			original_children = configFile.getList(currentPath);
			
			if(original_children == null) {
				OtherBlocks.logWarning("(loadSpecificFileVersion) Block \""+currentKey+"\" has no children. Have you included the dash?");
				continue;
			}

			currentKey = currentKey.toUpperCase();
			DropsList dropGroups = new DropsList();
			if (version == 1) {
				for(Object o : original_children) {
					if(o instanceof HashMap<?,?>) {


						CustomDrop drop = readTool(currentKey, o, configFile);

						if (!(drop == null)) {
							DropGroup drops = new DropGroup();
							drops.list.add(drop);
							dropGroups.list.add(drops);
						}
					}
				}
			} else if (version == 2) {
				dropGroups = readBlock(currentPath, configFile, currentKey);
			}

			// new hash map for more efficient comparisons

			Object blockId = null;
			// Source block
			blockId = getBlockId(currentKey);
			
			if (blockId != null) { 
				if (blockId instanceof String) {
					addToDropHash(blockId.toString(), dropGroups);
				} else {
					List<Material> listMats = (List<Material>)blockId;
					String blockString;
					for (Material mat : listMats) {
						Integer blockInt = mat.getId();
						blockString = blockInt.toString();
						addToDropHash(blockString, dropGroups);
					}

				}
			}

		}
		OtherBlocks.logInfo("CONFIG: "+filename+" loaded.",2);
	}
	
	protected Object getBlockId(String currentKey) {
		currentKey = currentKey.toUpperCase();
		String blockId = null;
		String blockString = getDataEmbeddedBlockString(currentKey);
		try {
			Integer blockInt = Integer.valueOf(blockString);
			blockId = blockInt.toString();
		} catch(NumberFormatException x) {
			if(isCreature(blockString)) {
				blockId = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
			} else if(isPlayer(currentKey)) {
				blockId = "PLAYER";
			} else if(isPlayerGroup(currentKey)) {
				blockId = "PLAYER";
			} else if(isLeafDecay(blockString)) {
				blockId = "SPECIAL_LEAFDECAY";
			} else if(blockString.startsWith("CLICK")) {
				blockId = blockString;
			} else if(isSynonymString(blockString)) {
				if(!CommonMaterial.isValidSynonym(blockString)) {
					throw new IllegalArgumentException(blockString + " is not a valid synonym");
				} else {
					// add to each hash for id's here
					List<Material> listMats = CommonMaterial.getSynonymValues(blockString);
					return listMats;
				}
			} else {
				try {
					Integer blockInt = Material.getMaterial(blockString).getId();
					blockId = blockInt.toString();
				} catch(Throwable ex) {
					OtherBlocks.logWarning("Configread: error getting matId for "+blockString);
				}
			}
		}
		return blockId;
	}

	private void addToDropHash(String blockId, DropsList dropGroups) {			
		if (blockId != null) {
			// check for existing container at this ID and add to it if there is
			DropsList thisDropGroups = blocksHash.get(blockId);
			if (thisDropGroups != null) {
				for (DropGroup dropGroup : dropGroups.list) {
					thisDropGroups.list.add(dropGroup);
				}
				OtherBlocks.logInfo("CONFIG: adding to existing blocksHash for: ("+blockId+")",3);
				blocksHash.put(blockId, thisDropGroups);
			} else {
				blocksHash.put(blockId, dropGroups);
				OtherBlocks.logInfo("CONFIG: creating new blocksHash for: ("+blockId+")",3);
			}
		}
	}
	
	private DropsList readBlock(String currentPath, Configuration configFile, String blockName) {
			DropsList dropGroups = new DropsList();

			List<Object> blockChildren = configFile.getList(currentPath);

			if(blockChildren == null) {
				OtherBlocks.logWarning("(readblock) Block \""+currentPath+"\" has no children. Have you included the dash?");
				return null;
			}
			//for(String blockChild : blockChildren) {

			for(Object blockChild : blockChildren) {
				//logWarning("inside readblock loop");
				if(blockChild instanceof HashMap<?,?>) {
					try {
						HashMap<?, ?> m = (HashMap<?, ?>) blockChild;
	
						if (m.get("dropgroup") != null) {
							OtherBlocks.logInfo("readBlock: adding dropgroup: " + String.valueOf(m.get("dropgroup")), 3);
							dropGroups.list.add(readDropGroup(m, configFile, blockName));
						} else {
							CustomDrop drop = readTool(blockName, blockChild, configFile);
							if (!(drop == null)) {
								OtherBlocks.logInfo("readBlock: adding single drop",3);
								DropGroup dropGroup = new DropGroup();
								dropGroup.tool = new ArrayList<String>();
								dropGroup.tool.add(null);
								
								dropGroup.list.add(drop);
								dropGroups.list.add(dropGroup);
							}
						}
					} catch(Throwable ex) {
						if(verbosity > 1) {
							OtherBlocks.logWarning("Error while processing dropgroup inside block '" + blockName + "' (" + ex.getMessage() + ")");
						}
		 
						if (verbosity > 2) ex.printStackTrace();
						return null;
					}

				}
			}
			return dropGroups;
		}

		private DropGroup readDropGroup(HashMap<?, ?> m, Configuration configFile, String blockName) throws Exception
		{
			DropGroup dropGroup = new DropGroup();

	//		List<Object> blockChildren = configFile.getList(currentPath);

//			if(blockChildren == null) {
	//			logWarning("Block \""+currentPath+"\" has no children. Have you included the dash?");
		//		return null;
			//}
			//for(String blockChild : blockChildren) {

		//	for(Object blockChild : blockChildren) {
			//	if(blockChild instanceof HashMap<?, ?>) {
				//	try{

					//	HashMap<?, ?> m = (HashMap<?, ?>) blockChild;
							OtherBlocks.logInfo("CONFIG: IN DROPGROUP....",3);

						//if (m.get("dropgroup") == null) {
					//		dropGroup.list.add(readTool(blockName, blockChild, configFile));
					//	} else {
							String name = (String) m.get("dropgroup");
							OtherBlocks.logInfo("Dropgroup found ("+name+")", 2);
							dropGroup.name = name;

							Double dropChance;
							try {
								dropChance = Double.valueOf(String.valueOf(m.get("chance")));
								dropGroup.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
							} catch(NumberFormatException ex) {
								dropGroup.chance = 100.0;
							}

							
							// Source block
							String s = blockName.toUpperCase();
							String blockString = getDataEmbeddedBlockString(s);

							dropGroup.original = null;
							try {
								dropGroup.original = blockString;
							} catch(NumberFormatException x) {
								if(isCreature(blockString)) {
									// Sheep can be coloured - check here later if need to add data vals to other mobs
									dropGroup.original = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
								} else if(isPlayer(s)) {
									dropGroup.original = s;
								} else if(isPlayerGroup(s)) {
									dropGroup.original = s;
								} else if(isLeafDecay(blockString)) {
									dropGroup.original = blockString;
								} else if(blockString.startsWith("CLICK")) {
									dropGroup.original = blockString.split("-")[1];
								} else if(isSynonymString(blockString)) {
									if(!CommonMaterial.isValidSynonym(blockString)) {
										throw new IllegalArgumentException(blockString + " is not a valid synonym");
									} else {
										dropGroup.original = blockString;
									}
								} else {
									dropGroup.original = Material.valueOf(blockString).toString();
								}
							}


							// Tool used
							dropGroup.tool = new ArrayList<String>();

								if (m.get("tool") == null) {
									dropGroup.tool.add(null); // set the default to ALL if not specified
								} else if(isLeafDecay(getDataEmbeddedBlockString(blockName))) {
									dropGroup.tool.add(null);
								} else if(m.get("tool") instanceof Integer) {
									Integer tool = (Integer) m.get("tool");
									dropGroup.tool.add(tool.toString());
								} else if(m.get("tool") instanceof String) {
									String toolString = (String) m.get("tool");
									if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

									if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
										dropGroup.tool.add(null);
									} else if(CommonMaterial.isValidSynonym(toolString)) {
										dropGroup.tool.add(toolString);
									} else if(isDamage(toolString) || isCreature(toolString)) {
										dropGroup.tool.add(toolString);
									} else if (toolString.contains("@")) {
										String[] toolSplit = toolString.split("@");
										dropGroup.tool.add(Material.valueOf(toolSplit[0].toUpperCase()).toString()+"@"+toolSplit[1]);
									} else {
										dropGroup.tool.add(Material.valueOf(toolString.toUpperCase()).toString());
									}
								} else if (m.get("tool") instanceof List<?>) {

									for(Object listTool : (List<?>) m.get("tool")) {
										String t = (String) listTool;
										if(CommonMaterial.isValidSynonym(t)) {
											dropGroup.tool.add(t);
										} else if(isDamage(t)) {
											dropGroup.tool.add(t);
										//} else if(isCreature(t)) {
										//	  dropGroup.tool.add(t);
										} else {
											dropGroup.tool.add(Material.valueOf(t.toUpperCase()).toString());
										}
									}

								} else {
									throw new Exception("Tool: Not a recognizable type");
								}

							// Tool EXCEPTIONS

							if (m.get("toolexcept") == null) {
								dropGroup.toolExceptions = null;
							} else {
								dropGroup.toolExceptions = new ArrayList<String>();
								if(isLeafDecay(getDataEmbeddedBlockString(blockName))) {
									dropGroup.toolExceptions.add(null);
								} else if(m.get("toolexcept") instanceof String) {

									String toolString = (String) m.get("toolexcept");
									toolString = toolString.toUpperCase();

									if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

									if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
										dropGroup.toolExceptions.add(null);
									} else if(CommonMaterial.isValidSynonym(toolString)) {
										dropGroup.toolExceptions.add(toolString);
									} else if(isDamage(toolString) || isCreature(toolString)) {
										dropGroup.toolExceptions.add(toolString);
									} else {
										dropGroup.toolExceptions.add(Material.valueOf(toolString).toString());
									}

								} else if (m.get("toolexcept") instanceof List<?>) {

									for(Object listTool : (List<?>) m.get("toolexcept")) {
										String t = (String) listTool;
										t = t.toUpperCase();
										if(CommonMaterial.isValidSynonym(t)) {
											dropGroup.toolExceptions.add(t);
										} else if(isDamage(t)) {
											dropGroup.toolExceptions.add(t);
											//} else if(isCreature(t)) {
											//	  dropGroup.tool.add(t);
										} else {
											dropGroup.toolExceptions.add(Material.valueOf(t).toString());
										}
									}

								} else {
									throw new Exception("Toolexcept: Not a recognizable type");
								}
							}

							// Applicable worlds
							String getString;

							getString = "world";
							if (m.get(getString) == null) getString = "worlds";															
							dropGroup.worlds = getArrayList(m.get(getString), true);
							if (dropGroup.worlds == null) {
								if (defaultWorlds == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.worlds = defaultWorlds;
								}
							}

							// Get applicable weather conditions
							dropGroup.weather = getArrayList(m.get("weather"), true);
							if (dropGroup.weather == null) {
								if (defaultWeather == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.weather = defaultWeather;
								}
							}

							 // Get faces
							dropGroup.faces = getArrayList(m.get("face"), true);
							if (dropGroup.faces == null) {
								throw new Exception("Not a recognizable type");
							}

							dropGroup.faces = getArrayList(m.get("face"), true);
							dropGroup.facesExcept = getArrayList(m.get("faceexcept"), true);

							// Get applicable biome conditions
							getString = "biome";
							if (m.get(getString) == null) getString = "biomes";															
							dropGroup.biome = getArrayList(m.get(getString), true);
							if (dropGroup.biome == null) throw new Exception("Not a recognizable type");
							if (dropGroup.biome == null) {
								if (defaultBiomes == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.biome = defaultBiomes;
								}
							}

							// Get event conditions
							dropGroup.event = getArrayList(m.get("event"), true);
							if (dropGroup.event == null) throw new Exception("Not a recognizable type");

							 // Get commands action
							dropGroup.commands = getArrayList(m.get("runcommands"), true);
							if (dropGroup.commands == null) throw new Exception("Not a recognizable type");

							// Message
							// Applicable messages
							getString = "message";
							if (m.get(getString) == null) getString = "messages";															
							dropGroup.messages = getArrayList(m.get(getString), false);
							if (dropGroup.messages == null) throw new Exception("Not a recognizable type");

							// Get the time string
							String timeString = String.valueOf(m.get("time"));
							if(m.get("time") == null) {
								dropGroup.time = defaultTime;
							} else {
								dropGroup.time = timeString;
							}

							// Get the exclusive string
							String exlusiveString = String.valueOf(m.get("exclusive"));
							if(m.get("exclusive") == null) {
								dropGroup.exclusive = null;
							} else {
								dropGroup.exclusive = exlusiveString;
							}

							// Get permission groups
							dropGroup.permissionGroups = getArrayList(m.get("permissiongroup"), true);
							if (dropGroup.permissionGroups == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionGroups == null) {
								if (defaultPermissionGroups == null) {
									throw new Exception("Not a recognizable type");
								} else {
									OtherBlocks.logWarning("permissionsgroup is obselete - please use 'permissions' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
									dropGroup.permissionGroups = defaultPermissionGroups;
								}
							}

							// Get permission groups
							dropGroup.permissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
							if (dropGroup.permissionGroupsExcept == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionGroupsExcept == null) {
								if (defaultPermissionGroupsExcept == null) {
									throw new Exception("Not a recognizable type");
								} else {
									OtherBlocks.logWarning("permissionsgroupexcept is obselete - please use 'permissionsExcept' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
									dropGroup.permissionGroupsExcept = defaultPermissionGroupsExcept;
								}
							}

							// Get permissions
							dropGroup.permissions = getArrayList(m.get("permissions"), true);
							if (dropGroup.permissions == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissions == null) {
								if (defaultPermissions == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.permissions = defaultPermissions;
								}
							}

							// Get permission exceptions
							dropGroup.permissionsExcept = getArrayList(m.get("permissionsExcept"), true);
							if (dropGroup.permissionsExcept == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionsExcept == null) {
								if (defaultPermissionsExcept == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.permissionsExcept = defaultPermissionsExcept;
								}
							}

							String heightString = String.valueOf(m.get("height"));
							if(m.get("height") == null) {
								dropGroup.height = null;
							} else {
								dropGroup.height = heightString;
							}

							String lightLevelString = String.valueOf(m.get("lightlevel"));
							if(m.get("lightlevel") == null) {
								dropGroup.lightLevel = null;
							} else {
								dropGroup.lightLevel = lightLevelString;
							}
							
							dropGroup.regions = getArrayList(m.get("regions"), true);
							if (dropGroup.regions == null) {
									throw new Exception("Not a recognizable type");
							}
							

							if (m.get("drops") != null) {
								List<Object> dropGroupDrops = (List<Object>) m.get("drops");

							if(dropGroupDrops == null) {
								OtherBlocks.logWarning("Dropgroup drops for \""+blockName+"."+name+"\" has no children. Have you included the dash?");
								return null;
							}
							//for(String blockChild : blockChildren) {
							OtherBlocks.logWarning("Reading dropgroup drops...",4);

							for(Object dropGroupChild : dropGroupDrops) {
								if(dropGroupChild instanceof HashMap<?, ?>) {
									try{
										
										OB_Drop toolContainer = readTool(blockName, dropGroupChild, configFile);
										dropGroup.list.add(toolContainer);
									} catch(Throwable ex) {
										if(verbosity > 1) {
											OtherBlocks.logWarning("DROPGROUP: Error while processing dropgroup drops " + blockName + ": " + ex.getMessage());
										}

										ex.printStackTrace();
										return null;
									}
								}
							}
							if (dropGroup.name != null) OtherBlocks.logInfo("dropgroup with name completed", 2);
							}
						



			return dropGroup; 
		}
		
		private CustomDrop readTool(String s, Object o, Configuration configFile) {	
			CustomDrop bt = new CustomDrop();

			try {
				HashMap<?, ?> m = (HashMap<?, ?>) o;
				String dropFromType = "";

				// Source block
				s = s.toUpperCase();
				String blockString = getDataEmbeddedBlockString(s);
				String dataString = getDataEmbeddedDataString(s);

				bt.original = null;
				bt.setData(null);
				try {
					Integer block = Integer.valueOf(blockString);
					bt.original = blockString;
					dropFromType = "BLOCK";
				} catch(NumberFormatException x) {
					if(isCreature(blockString)) {
						dropFromType = "CREATURE";
						// Sheep can be coloured - check here later if need to add data vals to other mobs
						bt.original = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
						if(blockString.contains("SHEEP")) {
							setDataValues(bt, dataString, "WOOL");
						} else {
							setDataValues(bt, dataString, blockString);
						}
					} else if(isPlayer(s)) {
						dropFromType = "CREATURE";
						bt.original = s;
					} else if(isPlayerGroup(s)) {
						dropFromType = "CREATURE";
						bt.original = s;
					} else if(isLeafDecay(blockString)) {
						dropFromType = "BLOCK";
						bt.original = blockString;
						setDataValues(bt, dataString, "LEAVES");
					} else if(blockString.startsWith("CLICK")) {
						bt.original = blockString.split("-")[1];
						// TODO: add data value support
					} else if(isSynonymString(blockString)) {
						dropFromType = "BLOCK";
						if(!CommonMaterial.isValidSynonym(blockString)) {
							throw new IllegalArgumentException(blockString + " is not a valid synonym");
						} else {
							bt.original = blockString;
						}
					} else {
						dropFromType = "BLOCK";
						bt.original = Material.valueOf(blockString).toString();
						setDataValues(bt, dataString, blockString);
					}
				}
				
				if (dropFromType.equalsIgnoreCase("BLOCK")) {
					OtherBlocksConfig.dropForBlocks = true;					
				} else if (dropFromType.equalsIgnoreCase("CREATURE")) {
					OtherBlocksConfig.dropForCreatures = true;					
				}

				// Tool used
				bt.setTool(new ArrayList<String>());

					if (m.get("tool") == null) {
						bt.getTool().add(null); // set the default to ALL if not specified
					} else if(isLeafDecay(bt.original)) {
						bt.getTool().add(null);
					} else if(m.get("tool") instanceof Integer) {
						Integer tool = (Integer) m.get("tool");
						bt.getTool().add(tool.toString());
					} else if(m.get("tool") instanceof String) {
						String toolString = (String) m.get("tool");
						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.getTool().add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.getTool().add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
							bt.getTool().add(toolString);
						} else if (toolString.contains("@")) {
							String[] toolSplit = toolString.split("@");
							bt.getTool().add(Material.valueOf(toolSplit[0].toUpperCase()).toString()+"@"+toolSplit[1]);
						} else {
							bt.getTool().add(Material.valueOf(toolString.toUpperCase()).toString());
						}
					} else if (m.get("tool") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("tool")) {
							String t = (String) listTool;
							if(CommonMaterial.isValidSynonym(t)) {
								bt.getTool().add(t);
							} else if(isDamage(t)) {
								bt.getTool().add(t);
							//} else if(isCreature(t)) {
							//	  bt.tool.add(t);
							} else {
								bt.getTool().add(Material.valueOf(t.toUpperCase()).toString());
							}
						}

					} else {
						throw new Exception("Tool: Not a recognizable type");
					}

				// Tool EXCEPTIONS

				if (m.get("toolexcept") == null) {
					bt.setToolExceptions(null);
				} else {
					bt.setToolExceptions(new ArrayList<String>());
					if(isLeafDecay(bt.original)) {
						bt.getToolExceptions().add(null);
					} else if(m.get("toolexcept") instanceof String) {

						String toolString = (String) m.get("toolexcept");
						toolString = toolString.toUpperCase();

						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.getToolExceptions().add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.getToolExceptions().add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
							bt.getToolExceptions().add(toolString);
						} else {
							bt.getToolExceptions().add(Material.valueOf(toolString).toString());
						}

					} else if (m.get("toolexcept") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("toolexcept")) {
							String t = (String) listTool;
							t = t.toUpperCase();
							if(CommonMaterial.isValidSynonym(t)) {
								bt.getToolExceptions().add(t);
							} else if(isDamage(t)) {
								bt.getToolExceptions().add(t);
								//} else if(isCreature(t)) {
								//	  bt.tool.add(t);
							} else {
								bt.getToolExceptions().add(Material.valueOf(t).toString());
							}
						}

					} else {
						throw new Exception("Toolexcept: Not a recognizable type");
					}
				}

				// Dropped item
				String fullDropString = String.valueOf(m.get("drop")).toUpperCase();
				
				String dropEmbeddedChance = getDropEmbeddedChance(fullDropString);
				String dropEmbeddedQuantity = getDropEmbeddedQuantity(fullDropString);
				if (fullDropString.split("/").length > 1) {
					fullDropString = fullDropString.split("/")[0];
				}
				
				String dropString = getDataEmbeddedBlockString(fullDropString);
				String dropDataString = getDataEmbeddedDataString(fullDropString);

				try {
					Integer block = Integer.valueOf(fullDropString);
					bt.setDropped(fullDropString);
				} catch(NumberFormatException x) {
					if(dropString.equalsIgnoreCase("DYE")) dropString = "INK_SACK";
					if(dropString.equalsIgnoreCase("NOTHING")) dropString = "AIR";

					if (m.get("drop") == null) {
						bt.setDropped("DEFAULT");
					} else if (dropString.startsWith("MONEY")) {
						bt.setDropped(dropString);
					} else if(isCreature(dropString)) {
						bt.setDropped("CREATURE_" + CreatureType.valueOf(creatureName(dropString)).toString());
						setDropDataValues(bt, dropDataString, dropString);
					} else if(dropString.equalsIgnoreCase("CONTENTS")) {
						bt.setDropped("CONTENTS");
					} else if(dropString.equalsIgnoreCase("DEFAULT")) {
						bt.setDropped("DEFAULT");
					} else if(dropString.equalsIgnoreCase("DENY")) {
						bt.setDropped("DENY");
					} else if(dropString.equalsIgnoreCase("NODROP")) {
						bt.setDropped("NODROP");
					} else {
						bt.setDropped(Material.valueOf(dropString.toUpperCase()).toString());
						setDropDataValues(bt, dropDataString, dropString);
					}
				}

				bt.setAttackerDamage(0);
				if (m.get("damageattacker") != null) {
					try {
						Integer dropQuantity = Integer.valueOf(m.get("damageattacker").toString());
						bt.setAttackerDamage(dropQuantity.intValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("damageattacker"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setAttackerDamage(Integer.valueOf(split[0]), Integer.valueOf(split[1]));									
						} else {
							OtherBlocks.logWarning("[BLOCK: "+bt.original+"] Invalid damageAttacker - set to 0.",3);
						}
					}
				}
					

				// Dropped color
				String dropColor = String.valueOf(m.get("color"));

				if (m.get("color") != null) {
					bt.setDropData(CommonMaterial.getAnyDataShort(bt.getDropped(), dropColor));
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
							OtherBlocks.logWarning("[BLOCK: "+bt.original+"] Invalid quantity - set to 1.");
						}
					}
				} else if (dropEmbeddedQuantity != null) {
					// TODO: fix this duplicate code - lazy I know :/
					try {
						Double dropQuantity = Double.valueOf(dropEmbeddedQuantity);
						//log.info(dropQuantity.toString());
						bt.setQuantity(dropQuantity.floatValue());
					} catch(NumberFormatException x) {
						String dropQuantity = dropEmbeddedQuantity;
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setQuantity(Float.valueOf(split[0]), Float.valueOf(split[1]));									
						} else {
							OtherBlocks.logWarning("[BLOCK: "+bt.original+"] Invalid quantity - set to 1.");
						}
					}
				}

				// Tool damage
				Integer toolDamage = Integer.class.cast(m.get("damagetool"));
				if (toolDamage == null) {
					toolDamage = Integer.class.cast(m.get("damage"));
					if (toolDamage != null) OtherBlocks.logWarning("'damage' is obselete, use 'damagetool'");
				}
				bt.damage = (toolDamage == null || toolDamage < 0) ? null : toolDamage;


				// Delay
				bt.setDelay(0);
				if (m.get("delay") != null) {
					try {
						Integer dropQuantity = Integer.valueOf(m.get("delay").toString());
						bt.setDelay(dropQuantity.intValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("delay"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setDelay(Integer.valueOf(split[0]), Integer.valueOf(split[1]));									
						} else {
							OtherBlocks.logWarning("[BLOCK: "+bt.original+"] Invalid delay - set to 0.",3);
						}
					}
				}

				
				// Drop probability
				Double dropChance;
				try {
					String dropChanceString = "";
					if (dropEmbeddedChance != null) {
						dropChanceString = dropEmbeddedChance.replaceAll("%", "").replaceAll("$", "");						
					} else {
						dropChanceString = String.valueOf(m.get("chance")).replaceAll("%", "").replaceAll("$", "");
					}
					dropChance = Double.valueOf(dropChanceString);
					bt.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
				} catch(NumberFormatException ex) {
					bt.chance = 100.0;
				}
				
				// Drop spread probability
				Double dropSpread;
				try {
					Object dropSpreadObj = m.get("dropspread");
					if (dropSpreadObj == null) {
						bt.setDropSpread(100.0);
					} else if (m.get("dropspread") instanceof Boolean) {
						Boolean dropSpreadBool = (Boolean)dropSpreadObj;
						if (!dropSpreadBool) {
							bt.setDropSpread(0.0);
						} else {
							bt.setDropSpread(100.0);
						}
					} else {
						dropSpread = Double.valueOf(String.valueOf(m.get("dropspread")));
						bt.setDropSpread((dropSpread < 0 || dropSpread > 100) ? 100 : dropSpread);
					}
				} catch(NumberFormatException ex) {
					bt.setDropSpread(100.0);
				}

				// Applicable worlds
				String getString;
				
				getString = "world";
				if (m.get(getString) == null) getString = "worlds";															
				bt.setWorlds(getArrayList(m.get(getString), true));
				if (bt.getWorlds() == null) {
					if (defaultWorlds == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.setWorlds(defaultWorlds);
					}
				}

				bt.setRegions(getArrayList(m.get("regions"), true));
				if (bt.getRegions() == null) {
						throw new Exception("Not a recognizable type");
				}
				
				// Get applicable weather conditions
				bt.setWeather(getArrayList(m.get("weather"), true));
				if (bt.getWeather() == null) {
					if (defaultWeather == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.setWeather(defaultWeather);
					}
				}
				
				// Get faces
				bt.faces = getArrayList(m.get("face"), true);
				bt.facesExcept = getArrayList(m.get("faceexcept"), true);
				
				// Get replacementblock
				bt.replacementBlock = getArrayList(m.get("replacementblock"), true);

				// Get applicable biome conditions
				getString = "biome";
				if (m.get(getString) == null) getString = "biomes";															
				bt.setBiome(getArrayList(m.get(getString), true));
				if (bt.getBiome() == null) throw new Exception("Not a recognizable type");
				if (bt.getBiome() == null) {
					if (defaultBiomes == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.setBiome(defaultBiomes);
					}
				}

				// Get event conditions
				bt.event = getArrayList(m.get("event"), true);
				if (bt.event == null) throw new Exception("Not a recognizable type");

				// Get commands action
				bt.commands = getArrayList(m.get("runcommands"), true);
				if (bt.commands == null) throw new Exception("Not a recognizable type");

				// Message
				// Applicable messages
				getString = "message";
				if (m.get(getString) == null) getString = "messages";															
				bt.messages = getArrayList(m.get(getString), false);
				if (bt.messages == null) throw new Exception("Not a recognizable type");

				// Get the time string
				String timeString = String.valueOf(m.get("time"));
				if(m.get("time") == null) {
					bt.setTime(defaultTime);
				} else {
					bt.setTime(timeString);
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
						OtherBlocks.logWarning("permissionsgroup is obselete - please use 'permissions' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
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
						OtherBlocks.logWarning("permissionsgroupexcept is obselete - please use 'permissionsExcept' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
											bt.permissionGroupsExcept = defaultPermissionGroupsExcept;
					}
				}

								// Get permissions
								bt.permissions = getArrayList(m.get("permissions"), true);
								if (bt.permissions == null) throw new Exception("Not a recognizable type");
								if (bt.permissions == null) {
										if (defaultPermissions == null) {
												throw new Exception("Not a recognizable type");
										} else {
												bt.permissions = defaultPermissions;
										}
								}
								
								// Get permission exceptions
								bt.permissionsExcept = getArrayList(m.get("permissionsExcept"), true);
								if (bt.permissionsExcept == null) throw new Exception("Not a recognizable type");
								if (bt.permissionsExcept == null) {
										if (defaultPermissionsExcept == null) {
												throw new Exception("Not a recognizable type");
										} else {
												bt.permissionsExcept = defaultPermissionsExcept;
										}
								}

								bt.height = mGetString(m, "height");
								bt.attackRange = mGetString(m, "attackrange");
								
								String lightLevelString = String.valueOf(m.get("lightlevel"));
								if(m.get("lightlevel") == null) {
									bt.lightLevel = null;
								} else {
									bt.lightLevel = lightLevelString;
								}

 
			} catch(Throwable ex) {
				OtherBlocks.logWarning("Error while processing block '" + s + "' (" + ex.getMessage() + ")", 1); 
				if (verbosity > 2) ex.printStackTrace();
				return null;
			}

			if(verbosity > 1) {
				OtherBlocks.logInfo("BLOCK: " +
						(bt.getTool().contains(null) ? "ALL TOOLS" : (bt.getTool().size() == 1 ? bt.getTool().get(0).toString() : bt.getTool().toString())) + " + " +
						creatureName(bt.original) + bt.getData() + " now drops " +
						(bt.getQuantityRange() + "x ") + 
						creatureName(bt.getDropped()) + "@" + bt.getDropDataRange() +
						(bt.chance < 100 ? " with " + bt.chance.toString() + "% chance" : "") +
						(!bt.getRegions().contains(null) ? " in regions " + bt.getRegions().toString() + " only ": ""));
			}

			return bt;

		}
		
		String mGetString (HashMap<?, ?> m, String param) {
			String heightString = String.valueOf(m.get(param));
			if(m.get(param) == null) {
				return null;
			} else {
				return heightString;
			}
						}
	}