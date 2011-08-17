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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

//import org.bukkit.*;
//import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.Jukebox;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

import com.gmail.zariust.bukkit.common.*;
import com.gmail.zariust.bukkit.otherblocks.drops.*;
import com.gmail.zariust.bukkit.otherblocks.listener.*;
import com.gmail.zariust.register.payment.Method;
import com.gmail.zariust.register.payment.Method.MethodAccount;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class OtherBlocks extends JavaPlugin
{
	public PluginDescriptionFile info = null;

	private static Logger log;

	public Map<Entity, String> damagerList;
	protected Random rng;

	// Config stuff
	public OtherBlocksConfig config = null;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;

	// Listeners
	private final OtherBlocksBlockListener blockListener;
	private final OtherBlocksEntityListener entityListener;
	private final OtherBlocksVehicleListener vehicleListener;
	private final OtherBlocksPlayerListener playerListener;

	// for Register (economy support)
	public static Method method = null;

	// for LogBlock support
	public static Consumer lbconsumer = null;
	
	// for BigBrother support
	//public static BigBrother bigBrother = null;

	// for Permissions support
	public static PermissionHandler permissionHandler = null;
	public static PermissionHandler worldguardHandler;

	// for WorldGuard support
	public static WorldGuardPlugin worldguardPlugin;

	
	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static OtherBlocks plugin;

	public static HashMap<String, List<Long>> profileMap;
	
	private static PlayerWrapper playerCommandExecutor;

	
	// LogInfo & Logwarning - display messages with a standard prefix
	static void logWarning(String msg) {
		log.warning("["+pluginName+":"+pluginVersion+"] "+msg);
	}
	public static void logInfo(String msg) {
		log.info("["+pluginName+":"+pluginVersion+"] "+msg);
	}

	// LogInfo & LogWarning - if given a level will report the message
	// only for that level & above
	public static void logInfo(String msg, Integer level) {
		if (OtherBlocksConfig.verbosity >= level) logInfo(msg);
	}
	static void logWarning(String msg, Integer level) {
		if (OtherBlocksConfig.verbosity >= level) logWarning(msg);
	}

	// Setup access to the permissions plugin if enabled in our config file
	// TODO: would be simple to create a dummy permissions class (returns true for all has() and false for ingroup()) so we don't need to 
	// keep checking if permissions is null
	void setupPermissions(boolean useYeti) {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (useYeti) {
			if (OtherBlocks.permissionHandler == null) {
				if (permissionsPlugin != null) {
					OtherBlocks.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					if (OtherBlocks.permissionHandler != null) {
						System.out.println("[OtherBlocks] hooked into Permissions.");
					} else {
						System.out.println("[OtherBlocks] cannot hook into Permissions - failed.");
					}
				} else {
					// TODO: read ops.txt file if Permissions isn't found.
					System.out.println("[OtherBlocks] Permissions not found.  Permissions disabled.");
				}
			}
		} else {
			System.out.println("[OtherBlocks] Permissions not enabled in config.");
			permissionHandler = null;
		}

	}

	/**
	 * Setup WorldGuardAPI - hook into the plugin if it's available
	 */
	private void setupWorldGuard() {
		OtherBlocks.worldguardPlugin = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (OtherBlocks.worldguardPlugin == null) {
			OtherBlocks.logInfo("Couldn't load WorldGuard.");
		} else {
			OtherBlocks.logInfo("Hooked into WorldGuard.");			
		}
	}

	public OtherBlocks() {

		blockListener = new OtherBlocksBlockListener(this);
		entityListener = new OtherBlocksEntityListener(this);
		vehicleListener = new OtherBlocksVehicleListener(this);
		playerListener = new OtherBlocksPlayerListener(this);
		
		// this list is used to store the last entity to damage another entity (along with the weapon used and range, if applicable)
		damagerList = new HashMap<Entity, String>();
		
		// this is used to store profiling information (milliseconds taken to complete function runs)
		profileMap = new HashMap<String, List<Long>>();
		profileMap.put("DROP", new ArrayList<Long>());
		profileMap.put("LEAFDECAY", new ArrayList<Long>());
		profileMap.put("BLOCKBREAK", new ArrayList<Long>());

			
		rng = new Random();
		log = Logger.getLogger("Minecraft");

		verbosity = 2;
		pri = Priority.Lowest;
	}

	public boolean hasPermission(Player player, String permission) {
		if (permissionHandler == null)
			return player.hasPermission(permission);
		else return permissionHandler.has(player, permission);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		// ******************
		// ** Reload
		// ******************
	if (label.equalsIgnoreCase("otherblocksreload") || label.equalsIgnoreCase("obr")) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (hasPermission(player, "otherblocks.admin.reloadconfig")) {
				config.reload();
				player.sendMessage("Otherblocks config reloaded.");				
				OtherBlocks.logInfo("Config reloaded by "+player.getName());
			} else {
					player.sendMessage("You don't have permission for that command.");
			}
		} else {
			config.reload();
			OtherBlocks.logInfo("Config reloaded by CONSOLE.");
		}
	} else if (label.equalsIgnoreCase("ob")) {
		// ******************
		// ** Show
		// ******************
		if (args.length == 0) return true;
		
		if (args[0].equalsIgnoreCase("show")) {
			String blockname = null;
			if (args.length > 1) {
				blockname = args[1];
			} else {
				sendMessagePlayerOrConsole(sender, "Error, no block. Please use /ob show <block>");
				return true;
			}
			
			Boolean hasPermission = false;
			
			if (sender instanceof Player) {
				Player player = (Player)sender;
				if (permissionHandler != null) {
					if (permissionHandler.has(player, "otherblocks.admin.show")) {
						hasPermission = true;
					}				
				} else {
					if (player.isOp()) {
						hasPermission = true;
					}			
				}
			} else {
				hasPermission = true;
			}
			
			if (!hasPermission) {
				sendMessagePlayerOrConsole(sender, "You don't have permission for that command.");
				return true;
			}
			
			// Permissions passed
			Object blockId = config.getBlockId(blockname);
			if (blockId instanceof String) blockname = (String)blockId;
			
			showBlockInfo(sender, blockname, true);
			showBlockInfo(sender, "CLICKLEFT-"+blockname, false);
			showBlockInfo(sender, "CLICKRIGHT-"+blockname, false);
		} else if (args[0].equalsIgnoreCase("profile")) {
			profilingCommand(sender, args);
		}
	}

	return true;		
	}

	
	/** "/ob profile" command - turns profiling on/off or shows profile information for particular event.
	 * 
	 * @param sender CommandSender from Bukkit onCommand() function - can be a player or console
	 * @param args   String list of command arguments from Bukkit onCommand() function
	 */
	public void profilingCommand(CommandSender sender, String[] args) {
	    if (args.length < 2) {
			// TODO: show usage
	        sendMessagePlayerOrConsole(sender, "Usage: /ob profile <cmd> (cmd = on/off/leafdecay/blockbreak/entitydeath)");
	        return;
	    }
	    
	    if (args[1].equalsIgnoreCase("off")) {
	        OtherBlocksConfig.profiling = false;
	        for (String profile : OtherBlocks.profileMap.keySet())
	        {
	            OtherBlocks.profileMap.get(profile).clear();
	        }
	        sendMessagePlayerOrConsole(sender,"Profiling stopped, profiling data cleared.");
	    } else if (args[1].equalsIgnoreCase("on")){
	        OtherBlocksConfig.profiling = true;
	        sendMessagePlayerOrConsole(sender, "Profiling started...");
	    } else {
	        if (OtherBlocksConfig.profiling) {
    	        List<Long> profileData = OtherBlocks.profileMap.get(args[1].toUpperCase());
    	        if (profileData == null) {
    	            sendMessagePlayerOrConsole(sender, "No data found.");   
    	        } else {
    	            Boolean showAverage = false;
    	            if (args.length >= 3) {
    	                if (args[2].equalsIgnoreCase("avg")) showAverage = true;
    	            }
    	            if (showAverage) {
    	                Long average = (long)0;
    	                Long total = (long)0;
    	                for (Long profileBit : profileData) {
    	                    total = total + profileBit;
    	                }
    	                average = total / profileData.size();
    	                sendMessagePlayerOrConsole(sender, "average: "+average.toString());
    	            } else {
    	                sendMessagePlayerOrConsole(sender, profileData.toString());
    	            }
    	        }
	        } else {
	            sendMessagePlayerOrConsole(sender, "Profiling is currently off - please turn on with /ob profile on");
	        }
	    }
	}
	
	
	/** "/ob show" command - shows conditions and actions for the specified block
	 * 
	 * @param sender            CommandSender from Bukkit onCommand() function - can be a player or console
	 * @param blockname         Name of the block whose info we want to show
	 * @param showNoInfoMessage Alert commandersender if no info found? Use "false" if commandersender is a player without permissions to use this command.
	 */
	public void showBlockInfo(CommandSender sender, String blockname, Boolean showNoInfoMessage) {
		String message = "Block ("+blockname+"): ";

		DropsList dropGroups = config.blocksHash.get(blockname);
		
		if (dropGroups != null) {
			for (DropGroup drops : dropGroups.list) {
				String dropName = (drops.getName() == null) ? "#" : drops.getName();
				message = message + "dropgroup: "+dropName;
				for (CustomDrop drop : drops.getDrops()) {
					message = message + " with: "+(drop.getTool().contains(null) ? "ANY" : drop.getTool().toString());
					message = message + " drops: "+drop.getDropped() + (drop.getDropDataRange().isEmpty() ? "" : "@"+drop.getDropDataRange());
					message = message + " ("+drop.chance+"%)";
					message = message + (drop.getRegions().contains(null) ? "": " regions: "+drop.getRegions().toString());
					message = message + (drop.event.contains(null) ? "": " event: "+drop.event.toString());						
					message = message + (drop.permissions.contains(null) ? "": " permissions: "+drop.permissions.toString());						
					message = message + (drop.getWorlds().contains(null) ? "": " worlds: "+drop.getWorlds().toString());						
					message = message + (drop.messages.contains(null) ? "": " message: "+drop.messages.toString());						
					message = message + (drop.faces.contains(null) ? "": " face: "+drop.faces.toString());					   
					message = message + (drop.replacementBlock.contains(null) ? "": " replacementblock: "+drop.replacementBlock.toString());						
					message = message + " | ";
				}
			}
			sendMessagePlayerOrConsole(sender, message);
		} else {
			if (showNoInfoMessage) sendMessagePlayerOrConsole(sender, message+"No info found.");
		}
	}
	
	
	/** If CommandSender is a player - send the message to them, otherwise log the message to the console.	
	 * 
	 * @param sender  CommandSender generally from Bukkit onCommand() - can be a player or the console
	 * @param message Message to be shown
	 */
	public void sendMessagePlayerOrConsole(CommandSender sender, String message) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.sendMessage(message);
		} else {
			OtherBlocks.logInfo(message);
		}

	}

	/** getFirstOp() - saves the name of the first op found in the ops.txt - for use in executing commands as an admin/op
	 * @author RabidCrab - used with permission - original code at https://github.com/RabidCrab/Minecraft.Vote
	 */
	public void getFirstOp()
	{
		// Pull the first name from ops.txt and use them to call functions
		FileInputStream fileStream = null;
		
		try
		{
			fileStream = new FileInputStream("ops.txt");
		} catch (FileNotFoundException e)
		{
			log.severe("Cannot find the ops file!");
			return;
		}
		
		// Get the object of DataInputStream
		DataInputStream dataStream = new DataInputStream(fileStream);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataStream));
		String firstOp;
		
		try
		{
			firstOp = bufferedReader.readLine();
			playerCommandExecutor = new PlayerWrapper(firstOp);
		} 
		catch (IOException e)
		{
			logWarning("Ops file is corrupt!");
			return;
		}
		
		if (playerCommandExecutor == null)
			logWarning("Can't find an op to mimic!");
	}
	
	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{		 
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		
		server = this.getServer();
		plugin = this;
		getDataFolder().mkdirs();

		getFirstOp();

		//setupPermissions();
		setupWorldGuard();

		// Register events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.PLUGIN_ENABLE, new OB_ServerListener(this), Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, new OB_ServerListener(this), Priority.Monitor, this);

		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, pri, this);
		pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, pri, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, pri, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, pri, this);
		pm.registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, pri, this); //*
		pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, pri, this); //*
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, pri, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, pri, this);

		// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
		if (this.enableBlockTo) {
			pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, pri, this); //*
		}

		// Register logblock plugin so that we can send break event notices to it
		final Plugin logBlockPlugin = pm.getPlugin("LogBlock");
		if (logBlockPlugin != null)
			lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();

		//bigBrother = (BigBrother) pm.getPlugin("BigBrother");
		
		config = new OtherBlocksConfig(this);
		config.load();
		logInfo("("+this.getDescription().getVersion()+") loaded.");
	}

	// If logblock plugin is available, inform it of the block destruction before we change it
	public static boolean queueBlockBreak(java.lang.String playerName, Block block)
	{
		org.bukkit.block.BlockState before = block.getState();
		String message = playerName+"-broke-"+block.getType().toString();
		
//		if (bigBrother != null) {
//			// Block Breakage
//			OtherBlocks.logInfo("Attempting to log to BigBrother: "+message, 4);
//			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
//		}
		
		if (lbconsumer != null) {
			OtherBlocks.logInfo("Attempting to log to LogBlock: "+message, 4);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		return true;
	}

	//
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
		return s.startsWith("SPECIAL_LEAFDECAY");
	}

	public static String creatureName(String s) {
		return (isCreature(s) ? s.substring(9) :s);
	}

	public static boolean hasDataEmbedded(String s) {
		return s.contains("@");
	}

	/** 
	 * @param s Original string that may or may not contain a data value.
	 * @return	Block name component of string (or same string as input, if "@" separator is not present)
	 */
	public static String getDataEmbeddedBlockString(String s) {
		if(!hasDataEmbedded(s)) return s;
		return s.substring(0, s.indexOf("@"));
	}

	public static String getDataEmbeddedDataString(String s) {
		if(!hasDataEmbedded(s)) return null;
		return s.substring(s.indexOf("@") + 1);
	}

	//
	// Useful longer functions
	//

	protected static void setDataValues(CustomDrop obc, String dataString, String objectString, Boolean dropData) {
		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			// TOFIX:: check for valid numbers - or is this checked earlier?
			if (dropData) {
				obc.setDropData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
			} else {
				obc.setData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
			}
		} else {
			if (dropData) {
				obc.setDropData(CommonMaterial.getAnyDataShort(objectString, dataString));
			} else {
				obc.setData(CommonMaterial.getAnyDataShort(objectString, dataString));
			}
		}
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

	


	/** Starts up the delayed (possible for 0 ticks) drop - calls performActualDrop() via a sync task.
	 * 
	 * @param target The location of the item being destroyed
	 * @param dropData The OB_Drop container of parameters for this drop
	 * @param player The player object (that destroyed this item)
	 */
	protected static void performDrop(Object target, CustomDrop dropData, Player player) {

		//if (dropData.delay > 0) {
		// TODO: fix if player = null
		Location playerLoc = null;
		if (player != null) playerLoc = player.getLocation();
		DropRunner dropRunner = new DropRunner(plugin, target, dropData, player, playerLoc);
		
		// schedule the task - NOTE: this must be a sync task due to the changes made in the performActualDrop function
		server.getScheduler().scheduleSyncDelayedTask(plugin, dropRunner, Long.valueOf(dropData.getRandomDelay()));			
		//}
			
	}
		
	/** Performs all actionable aspects of a drop - events, messages and the item drop itself.	This should be called from performDrop()
	 *	so that the drop.delay parameter can work.
	 * 
	 * @param target The location of the item being destroyed
	 * @param dropData The OB_Drop container of parameters for this drop
	 * @param player The player object (that destroyed this item)
	 * @param playerLoc Location of the player at the time that the item was destroyed (needed for delayed events sometimes)
	 */
	protected void performActualDrop(Object target, CustomDrop dropData, Player player, Location playerLoc) {
		Long currentTime = null; 
		if (OtherBlocksConfig.profiling) currentTime = System.currentTimeMillis();

		Location location = null;
		Entity entity = null;
		Block block = null;

		location = dropData.location;

		if (target instanceof Block) {
			block = (Block) target;
//			location = block.getLocation(); 
		} else if (target instanceof Entity) {
			entity = (Entity) target;
//			location = entity.getLocation();
		} else {
			OtherBlocks.logWarning("PerformActualDrop - Error: target type ("+target.toString()+") unknown - this shouldn't happen.");
			//return;
		}

		// Events
		Location treeLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
		
		// effects
	//	Effect effect = Effect.SMOKE;
	//	location.getWorld().playEffect(location, Effect.SMOKE, 90, 30);
	//	  location.getWorld().playEffect(location, Effect.CLICK2, 0);
	//	OtherBlocks.logInfo("played effect");
		
		for(String events : dropData.event) {
			if(events != null) {
				if(events.equalsIgnoreCase("EXPLOSION")) {
					//log.info("explosion!");
					location.getWorld().createExplosion(location, 4);
				} else if(events.startsWith("FORCETREE") || events.startsWith("TREE")) {
					OtherBlocks.logInfo("tree starting", 4);
					Integer origMat = null;
					byte origData = (byte)0;
					Block downBlock = null;
					if (events.startsWith("FORCE")) { 
						downBlock = treeLocation.getBlock().getRelative(BlockFace.DOWN);
						// allow replacing just some safe common materials (avoid items that have contents)
						if (downBlock.getType() != Material.CHEST && 
								downBlock.getType() != Material.FURNACE &&
								downBlock.getType() != Material.BURNING_FURNACE &&
								downBlock.getType() != Material.DISPENSER) {
							origMat = downBlock.getTypeId();
							origData = downBlock.getData();
							downBlock.setType(Material.DIRT);
						}
						events = events.replace("FORCETREE", "");
					} else {
						events = events.replace("TREE", "");
					}
					TreeType treeType = TreeType.TREE; 
					if (events != "") {
						try {
							treeType = TreeType.valueOf(events.substring(1));
						} catch (Exception ex) {}
					}
					location.getWorld().generateTree(treeLocation, treeType);
					if (origMat != null) downBlock.setTypeIdAndData(origMat, origData, false);
				} else if(events.equalsIgnoreCase("LIGHTNING")) {
					location.getWorld().strikeLightning(location);
				} else if(events.equalsIgnoreCase("LIGHTNING@HARMLESS")) {
					location.getWorld().strikeLightningEffect(location);
				} else if(events.equalsIgnoreCase("LIGHTNING@PLAYER")) {
					if (player != null) location.getWorld().strikeLightning(player.getLocation());					
				} else if(events.equalsIgnoreCase("LIGHTNING@HARMLESS@PLAYER")) {
					if (player != null) location.getWorld().strikeLightningEffect(player.getLocation());					
				} else if(events.equalsIgnoreCase("LIGHTNING@PLAYERLOCATION")) {
					if (player != null && playerLoc != null) location.getWorld().strikeLightning(playerLoc);					
				} else if(events.equalsIgnoreCase("LIGHTNING@HARMLESS@PLAYERLOCATION")) {
					if (player != null && playerLoc != null) location.getWorld().strikeLightningEffect(playerLoc);					
				} else if(events.equalsIgnoreCase("SHEAR")) {
					if (entity != null) {
						if (entity instanceof Sheep) {
							Sheep sheep = (Sheep) entity;
							sheep.setSheared(true);
						}
					}
				} else if(events.equalsIgnoreCase("UNSHEAR")) {
					if (entity != null) {
						if (entity instanceof Sheep) {
							Sheep sheep = (Sheep) entity;
							sheep.setSheared(false);
						}
					}
				} else if(events.equalsIgnoreCase("SHEARTOGGLE")) {
					if (entity != null) {
						if (entity instanceof Sheep) {
							Sheep sheep = (Sheep) entity;
							if (sheep.isSheared()) {
								sheep.setSheared(false);
							} else {
								sheep.setSheared(true);
							}
						}
					}
				}
			}
		}

		// Do actual drop

		String amountString = "unknown";

		// **************
		// DROP money
		// **************
		if (dropData.getDropped().equalsIgnoreCase("MONEY"))
		{
			if (player != null) {
				if (method.hasAccount(player.getName()))
				{
					MethodAccount account = method.getAccount(player.getName());
					Double amount = Double.valueOf(dropData.getRandomQuantityDouble()); 
					account.add(amount);
					amountString = amount.toString();
				}
			}
		// **************
		// DROP blocks
		// **************
		} else if(!isCreature(dropData.getDropped())) {
			if(!dropData.getDropped().equalsIgnoreCase("DEFAULT")) { 
				if(dropData.getDropped().equalsIgnoreCase("CONTENTS")) {
					doContentsDrop(location, dropData);
				} else { // Material should be valid - check for int value first, otherwise get material by string name
					Material dropMaterial = null;
					try {
						Integer originalInt = Integer.valueOf(dropData.getDropped());
						dropMaterial = Material.getMaterial(originalInt);
					} catch(NumberFormatException x) {
						dropMaterial = Material.valueOf(dropData.getDropped().toUpperCase());
					}
					// Special exemption for AIR - breaks the map! :-/
					if(dropMaterial != Material.AIR) {
						Integer amount = dropData.getRandomQuantityInt();
						amountString = amount.toString();
						if (amount != 0) { // 0 causes an "infinite" block that fills your inventory but can't be built)
							Short dropDataColor = dropData.getRandomDropData();
							if (dropDataColor == null) dropDataColor = 0;
							if(AbstractDrop.rng.nextDouble() > (dropData.getDropSpread() / 100)) {
								location.getWorld().dropItemNaturally(location, new ItemStack(dropMaterial, amount, dropDataColor));
							} else {
								for (int i = 0; i < amount; i++) {
									location.getWorld().dropItemNaturally(location, new ItemStack(dropMaterial, 1, dropDataColor));										
								}
							}
						}
					}
				}
			}
		// **************
		// DROP creatures
		// **************
		} else {
			Integer quantity = dropData.getRandomQuantityInt();
			amountString = quantity.toString();
			for(Integer i = 0; i < quantity; i++) {
				Entity critter = location.getWorld().spawnCreature(
						new Location(location.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5), 
						CreatureType.valueOf(OtherBlocks.creatureName(dropData.getDropped()))
				);
				String critterTypeName = CreatureType.valueOf(OtherBlocks.creatureName(dropData.getDropped())).toString();
				Short dataVal = dropData.getRandomDropData();

				if(critterTypeName == "PIG") {
					// @UNSADDLED=0, @SADDLED=1
					Pig pig = (Pig)critter;
					if (dataVal == (short)1) pig.setSaddle(true);
				} else if(critterTypeName == "WOLF") {
					// @NEUTRAL=0, @TAME=1, @ANGRY=2 
					Wolf wolf = (Wolf)critter;
					if (dataVal == (short)1) wolf.setTamed(true);
					if (dataVal == (short)2) wolf.setAngry(true);
				} else if(critterTypeName == "CREEPER") {
					// @UNPOWERED=0, @POWERED=1
					Creeper creeper = (Creeper)critter;
					if (dataVal == (short)1) creeper.setPowered(true);
				} else if(critter instanceof Colorable) { // SHEEP
					Colorable ccrit = (Colorable)critter;
					ccrit.setColor(DyeColor.getByData(dataVal.byteValue()));
				} else if(critterTypeName == "SLIME") {
					Slime slime = (Slime)critter;
					slime.setSize(dataVal);
				}


			}
		}

		// Show player (if any) a message (if set)
		sendPlayerRandomMessage(player, dropData.messages, amountString);
		

		// Run commands, if any
		if (dropData.commands != null) {
			try {
				boolean tempSuppressState = OtherBlocksConfig.runCommandsSuppressMessage;
				for (String command : dropData.commands) {								  
					if (command == null) continue;
					OtherBlocks.logInfo("Running command: "+command,4);
					
					command = command.replaceAll("%p", player.getName());
					if (command.startsWith("/")) command = command.replace("/", "");

					if (command.startsWith("sleep@")) {
						String sleepTimeString = command.replace("sleep@", "");
						try {
							Integer sleepTime = Integer.valueOf(sleepTimeString);
							if (sleepTime > 1000) sleepTime = 1000;
							Thread.sleep(sleepTime);
						} catch(NumberFormatException x) {}
						continue;
					} else if (command.startsWith("!")) {
						OtherBlocksConfig.runCommandsSuppressMessage = false;
						command = command.replace("!", "");
					}
					
					if ((!command.startsWith("*")) || isConsoleCommand(command))
						plugin.getServer().dispatchCommand(player, command);
					else
						plugin.getServer().dispatchCommand(OtherBlocks.getPlayerCommandExecutor(player), command.substring(1));

				}
				OtherBlocksConfig.runCommandsSuppressMessage = tempSuppressState;
			} catch (InterruptedException e) {
				OtherBlocks.logInfo(e.getMessage());
			}
		}
		
		if (currentTime != null) {
			OtherBlocks.logInfo("PerformActualDrop took "+(System.currentTimeMillis()-currentTime)+" milliseconds.",4);
			OtherBlocks.profileMap.get("DROP").add(System.currentTimeMillis()-currentTime);
		}
	}

	static void sendPlayerRandomMessage(Player player, List<String> messages, String amountString)
	{
		if (messages == null) return;
		if (messages.contains(null) || player == null) return;
		
		try {
			if (player != null) {
				if (messages != null) {
					// TOFIX:: not recommended to run two random number generators?	 better way of selecting random message?
					// - couldn't use this.rng due to this being a static function
					Random generator = new Random();
					int rnd = generator.nextInt(messages.size());
					String message = messages.get(rnd); // if message size = 1 then 

					message = message.replaceAll("%q", amountString);
					message = message.replaceAll("&([0-9a-fA-F])", "§$1"); //replace color codes
					message = message.replaceAll("&&", "&"); // replace "escaped" ampersand
					player.sendMessage(message);	
				}
			}
		} catch(Throwable ex){
		}
	}

	/**
	 * Courtesty of RabidCrab: If something is a console command, it gets executed differently from a player command
	 */
	private boolean isConsoleCommand(String command)
	{
		if (command.equalsIgnoreCase("kickall") 
				|| command.equalsIgnoreCase("stop") 
				|| command.equalsIgnoreCase("save-all"))
			return true;
		
		return false;
	}
	public static PlayerWrapper getPlayerCommandExecutor(Player caller)
	{
		if (caller != null) playerCommandExecutor.caller = caller;
		return playerCommandExecutor;
	}

	
	private static void doContentsDrop(Location target, CustomDrop dropData) {

		// Very odd - previous code of:
		// Furnace oven = (Furnace) target.getBlock().getState();
		// worked but now says I can't cast from BlockState to a Furnace.
		// Using blockState.getBlock() doesn't work either - says I can't
		// cast from craftBukkit.CraftBlock to Furnace.
		
		// Also odd - even though the event is cancelled and block removed the furnace still 
		// drops it's contents normally (even with just - drop: NOTHING)
		
		OtherBlocks.logWarning("CONTENTS drop is currently broken :(");
		
		List<ItemStack> drops = new ArrayList<ItemStack>();
		Inventory inven = null;
		Material mat = null;
		try {
			mat = Material.valueOf(dropData.original.toUpperCase());
		} catch (Exception ex) {}

		if (dropData.original.equalsIgnoreCase("ANY_FURNACE")) mat = Material.FURNACE;
		if (mat == null) return;
		
		switch(mat) {
		case FURNACE:
		case BURNING_FURNACE:
			BlockState blockState = target.getBlock().getState();
			Block block = blockState.getBlock();
			Furnace oven = (Furnace) block; 
//			Furnace oven = (Furnace) target.getBlock().getState();
			// Next three lines make you lose one of the item being smelted
			// Feel free to remove if you don't like that. -- Celtic Minstrel
			inven = oven.getInventory();
			ItemStack cooking = inven.getItem(0); // first item is the item being smelted
			if(oven.getCookTime() > 0) cooking.setAmount(cooking.getAmount()-1);
			if(cooking.getAmount() <= 0) inven.setItem(0, null);
			for (ItemStack i : inven.getContents()) drops.add(i);
			break;
		case DISPENSER:
			Dispenser trap = (Dispenser) target.getBlock().getState();
			inven = trap.getInventory();
			for (ItemStack i : inven.getContents()) drops.add(i);
			break;
		case CHEST: // Technically not needed, but included for completeness
			Chest box = (Chest) target.getBlock().getState();
			inven = box.getInventory();
			for (ItemStack i : inven.getContents()) drops.add(i);
			break;
		case STORAGE_MINECART: // Ditto
			StorageMinecart cart = null;
			for(Entity e : target.getWorld().getEntities()) {
				if(e.getLocation().equals(target) && e instanceof StorageMinecart)
					cart = (StorageMinecart) e;
			}
			if(cart != null) {
				inven = cart.getInventory();
				for (ItemStack i : inven.getContents()) drops.add(i);
			}
			break;
		case JUKEBOX:
			Jukebox jukebox = (Jukebox) target.getBlock().getState();
			drops.add(new ItemStack(jukebox.getPlaying()));
			break;
		}
	}

    // LogInfo & LogWarning - if given a level will report the message
    // only for that level & above
    static void logInfo(String msg, Integer level) {
        if (OtherBlocksConfig.verbosity >= level) logInfo(msg);
    }
    static void logWarning(String msg, Integer level) {
        if (OtherBlocksConfig.verbosity >= level) logWarning(msg);
    }
}
