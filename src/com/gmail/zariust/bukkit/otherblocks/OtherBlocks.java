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

import java.util.*;
import java.util.logging.Logger;

//import org.bukkit.*;
//import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.listener.*;
import com.gmail.zariust.bukkit.otherblocks.options.tool.Agent;
import com.gmail.zariust.register.payment.Method;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class OtherBlocks extends JavaPlugin
{
	public PluginDescriptionFile info = null;

	private static Logger log;

	public Map<Entity, Agent> damagerList;
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

	
	// LogInfo & Logwarning - display messages with a standard prefix
	public static void logWarning(String msg) {
		log.warning("["+pluginName+":"+pluginVersion+"] "+msg);
	}
	public static void logInfo(String msg) {
		log.info("["+pluginName+":"+pluginVersion+"] "+msg);
	}

	// LogInfo & LogWarning - if given a level will report the message
	// only for that level & above
	public static void logInfo(String msg, Integer level) {
		if (plugin.config.verbosity >= level) logInfo(msg);
	}
	public static void logWarning(String msg, Integer level) {
		if (plugin.config.verbosity >= level) logWarning(msg);
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
		damagerList = new HashMap<Entity, Agent>();
		
		// this is used to store profiling information (milliseconds taken to complete function runs)
		profileMap = new HashMap<String, List<Long>>();
		profileMap.put("DROP", new ArrayList<Long>());
		profileMap.put("LEAFDECAY", new ArrayList<Long>());
		profileMap.put("BLOCKBREAK", new ArrayList<Long>());
		
		rng = new Random();
		log = Logger.getLogger("Minecraft");
	}

	public boolean hasPermission(Permissible who, String permission) {
		if (permissionHandler == null)
			return who.hasPermission(permission);
		else {
			if(who instanceof Player)
				return permissionHandler.has((Player) who, permission);
			else return who.isOp();
		}
	}
	
	@Override
	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	@Override
	public void onEnable()
	{		 
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		
		server = this.getServer();
		plugin = this;
		getDataFolder().mkdirs();

		//setupPermissions();
		config = new OtherBlocksConfig(this);
		config.load();
		setupWorldGuard();

		// Register events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.PLUGIN_ENABLE, new OB_ServerListener(this), Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, new OB_ServerListener(this), Priority.Monitor, this);

		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, config.pri, this);
		pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, config.pri, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, config.pri, this);
		pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, config.pri, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, config.pri, this);
		
		this.getCommand("ob").setExecutor(new OtherBlocksCommand(this));

		// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
		if (this.enableBlockTo) {
			pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, config.pri, this); //*
		}

		// Register logblock plugin so that we can send break event notices to it
		final Plugin logBlockPlugin = pm.getPlugin("LogBlock");
		if (logBlockPlugin != null)
			lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();

		//bigBrother = (BigBrother) pm.getPlugin("BigBrother");
		
		logInfo("("+this.getDescription().getVersion()+") loaded.");
	}

	// If logblock plugin is available, inform it of the block destruction before we change it
	public boolean queueBlockBreak(java.lang.String playerName, Block block)
	{
		org.bukkit.block.BlockState before = block.getState();
		String message = playerName+"-broke-"+block.getType().toString();
		
//		if (bigBrother != null) {
//			// Block Breakage
//			OtherBlocks.logInfo("Attempting to log to BigBrother: "+message, 4);
//			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
//		}
		
		if (lbconsumer != null) {
			logInfo("Attempting to log to LogBlock: "+message, 4);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		return true;
	}
	
	public List<String> getGroups(Player player) {
		if(permissionHandler != null)
			return Arrays.asList(permissionHandler.getGroups(player.getWorld().getName(), player.getName()));
		List<String> foundGroups = new ArrayList<String>();
		Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
		for(PermissionAttachmentInfo perm : permissions) {
			String groupPerm = perm.getPermission();
			if(groupPerm.startsWith("group.")) foundGroups.add(groupPerm.substring(6));
			else if(groupPerm.startsWith("groups.")) foundGroups.add(groupPerm.substring(7));
		}
		return foundGroups;
	}
	
	public void performDrop(OccurredDrop drop) {
		// TODO The purpose of this method is to check the blocksHash for matches and perform them if found
		
	}
}
