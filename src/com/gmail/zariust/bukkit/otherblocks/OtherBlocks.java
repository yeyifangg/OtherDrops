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
import me.taylorkelly.bigbrother.BigBrother;

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
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

import com.gmail.zariust.bukkit.common.*;
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

	protected Map<Entity, String> damagerList;
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
	public static BigBrother bigBrother = null;

	// for Permissions support
	public static PermissionHandler permissionHandler = null;
	public static Plugin permissionsPlugin;
	public static PermissionHandler worldguardHandler;
	String permiss;
	public boolean usePermissions;

	// for WorldGuard support
	public static WorldGuardPlugin worldguardPlugin;

	
	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static OtherBlocks plugin;

    public static HashMap<String, List<Long>> profileMap;
    
    private static PlayerWrapper playerCommandExecutor;

		
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
    
    	// Load up the config - need to do this before registering events
        config = new OtherBlocksConfig(this);
        config.load();
    
    	// Register events
    	PluginManager pm = getServer().getPluginManager();
    
    	pm.registerEvent(Event.Type.PLUGIN_ENABLE, new OB_ServerListener(this), Priority.Monitor, this);
    	pm.registerEvent(Event.Type.PLUGIN_DISABLE, new OB_ServerListener(this), Priority.Monitor, this);
    
    	pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, OtherBlocksConfig.pri, this);
    	pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, OtherBlocksConfig.pri, this);
    	pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, OtherBlocksConfig.pri, this);
    	pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, OtherBlocksConfig.pri, this);
    	pm.registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, OtherBlocksConfig.pri, this); //*
    	pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, OtherBlocksConfig.pri, this); //*
    	pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, OtherBlocksConfig.pri, this);
    	pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, OtherBlocksConfig.pri, this);
    
    	// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
    	if (this.enableBlockTo) {
    		pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, OtherBlocksConfig.pri, this); //*
    	}
    
    	// Register logblock plugin so that we can send break event notices to it
    	final Plugin logBlockPlugin = pm.getPlugin("LogBlock");
    	if (logBlockPlugin != null)
    		lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();
    
    	bigBrother = (BigBrother) pm.getPlugin("BigBrother");
    	
    	logInfo("("+this.getDescription().getVersion()+") loaded.");
    }

    public void onDisable()
    {
    	log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
    }

	// Setup access to the permissions plugin if enabled in our config file
	// TODO: would be simple to create a dummy permissions class (returns true for all has() and false for ingroup()) so we don't need to 
	// keep checking if permissions is null
	void setupPermissions() {
		permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (usePermissions) {
			if (OtherBlocks.permissionHandler == null) {
				if (permissionsPlugin != null) {
					OtherBlocks.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					if (OtherBlocks.permissionHandler != null) {
						System.out.println("[OtherBlocks] hooked into Permissions.");
					} else {
						System.out.println("[OtherBlocks] cannot hook into Permissions - failed.");
					}
					permiss = "Yes";
				} else {
					// TODO: read ops.txt file if Permissions isn't found.
					System.out.println("[OtherBlocks] Permissions not found.  Permissions disabled.");
					permiss = "No";
				}
			}
		} else {
			System.out.println("[OtherBlocks] Permissions not enabled in config.");
			permiss = "No";        
			permissionsPlugin = null;
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

	public boolean hasPermission(Player player, String permission) {
		if (permissionHandler == null) {
			if (player.isOp()) return true;
		} else {
			if (permissionHandler.has(player, permission)) return true;
		}
		return false;
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

		OBContainer_DropGroups dropGroups = OtherBlocksConfig.blocksHash.get(blockname);
		
		if (dropGroups != null) {
			for (OBContainer_Drops drops : dropGroups.list) {
				String dropName = (drops.name == null) ? "#" : drops.name;
				message = message + "dropgroup: "+dropName;
				for (OB_Drop drop : drops.list) {
					message = message + " with: "+(drop.tool.contains(null) ? "ANY" : drop.tool.toString());
					message = message + " drops: "+drop.dropped + (drop.getDropDataRange().isEmpty() ? "" : "@"+drop.getDropDataRange());
					message = message + " ("+drop.chance+"%)";
					message = message + (drop.regions.contains(null) ? "": " regions: "+drop.regions.toString());
					message = message + (drop.event.contains(null) ? "": " event: "+drop.event.toString());						
					message = message + (drop.permissions.contains(null) ? "": " permissions: "+drop.permissions.toString());						
					message = message + (drop.worlds.contains(null) ? "": " worlds: "+drop.worlds.toString());						
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
	
	/**
	 * If logblock plugin is available, inform it of the block destruction before we change it.
	 * 
	 * @param playerName Name of player breaking the block
	 * @param block      Block that has been broken
	 * @return           Not currently used - always returns true (should return success or failure status?)
	 */
	public static boolean queueBlockBreak(java.lang.String playerName, Block block)
	{
		org.bukkit.block.BlockState before = block.getState();
		String message = playerName+"-broke-"+block.getType().toString();
		
		if (bigBrother != null) {
			// Block Breakage
			OtherBlocks.logInfo("Attempting to log to BigBrother: "+message, 4);
			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
		}
		
		if (lbconsumer != null) {
			OtherBlocks.logInfo("Attempting to log to LogBlock: "+message, 4);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		return true;
	}

	/**
	 * Simple get function for returning the "fake" player used to execute commands
	 * 
	 * @param caller Player object of the original player that messages should be sent to
	 * @return
	 */
    public static PlayerWrapper getPlayerCommandExecutor(Player caller)
    {
        if (caller != null) playerCommandExecutor.caller = caller;
        return playerCommandExecutor;
    }


    /** Starts up the delayed (possible for 0 ticks) drop - calls performActualDrop() via a sync task.
     * 
     * @param target   The location of the item being destroyed
     * @param dropData The OB_Drop container of parameters for this drop
     * @param player   The player object (that destroyed this item)
     */
    protected static void performDrop(Object target, OB_Drop dropData, Player player) {

        //if (dropData.delay > 0) {
        // TODO: fix if player = null
        Location playerLoc = null;
        if (player != null) playerLoc = player.getLocation();
        DropRunner dropRunner = new DropRunner(OtherBlocks.plugin, target, dropData, player, playerLoc);
        
        // schedule the task - NOTE: this must be a sync task due to the changes made in the performActualDrop function
        OtherBlocks.server.getScheduler().scheduleSyncDelayedTask(OtherBlocks.plugin, dropRunner, Long.valueOf(dropData.getRandomDelay()));         
        //}
            
    }
    
    /**
     * This simply passed the function call along to OtherBlocksDrops.performActualDrop as the DropRunner class cannot call a static function.
     * Would prefer to just use the OtherBlocksDrops class directly rather than cluttering up the OtherBlocks class
     * 
     * @param target
     * @param dropData
     * @param player
     * @param playerLoc
     */
    protected void performDrop_Passer(Object target, OB_Drop dropData, Player player, Location playerLoc) {
        OtherBlocksDrops.performActualDrop(target, dropData, player, playerLoc);
    }

    
    /**
     * logWarning - display a warning log message with a standard prefix
     * 
     * @param msg Message to be displayed
     */
    static void logWarning(String msg) {
        log.warning("["+pluginName+":"+pluginVersion+"] "+msg);
    }
    
    /**
     * logInfo - display an info log message with a standard prefix
     * 
     * @param msg Message to be displayed
     */
    static void logInfo(String msg) {
        log.info("["+pluginName+":"+pluginVersion+"] "+msg);
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
