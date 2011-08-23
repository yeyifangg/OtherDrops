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
import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import com.gmail.zariust.bukkit.otherblocks.drops.CustomDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.DropsList;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.listener.*;
import com.gmail.zariust.bukkit.otherblocks.subject.Agent;
import com.gmail.zariust.bukkit.otherblocks.subject.BlockTarget;
import com.gmail.zariust.bukkit.otherblocks.subject.PlayerSubject;
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

	// Config stuff
	public OtherBlocksConfig config = null;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;

	// Listeners
	private final ObBlockListener blockListener;
	private final ObEntityListener entityListener;
	private final ObVehicleListener vehicleListener;
	private final ObPlayerListener playerListener;
	private final ObServerListener serverListener;

	// for Register (economy support)
	public static Method method = null;

	// for LogBlock support
	public static Consumer lbconsumer = null;
	
	// for BigBrother support
	public static BigBrother bigBrother = null;

	// for Permissions support
	public static PermissionHandler permissionHandler = null;

	// for WorldGuard support
	public static WorldGuardPlugin worldguardPlugin;

	private static String pluginName;
	private static String pluginVersion;
	public static OtherBlocks plugin;

	private HashMap<String, ProfilerEntry> profileMap;
	
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
	void setupPermissions(boolean useYeti) {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		if (useYeti) {
			if (OtherBlocks.permissionHandler == null) {
				if (permissionsPlugin != null) {
					OtherBlocks.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					if (OtherBlocks.permissionHandler != null) {
						logInfo("Hooked into Permissions.");
					} else {
						logInfo("Cannot hook into Permissions - failed.");
					}
				} else {
					logInfo("Permissions not found.");
				}
			}
		} else {
			logInfo("Permissions not enabled in config.");
			permissionHandler = null;
		}
		if(permissionHandler == null) logInfo("Using Bukkit superperms.");
	}

	/**
	 * Setup WorldGuardAPI - hook into the plugin if it's available
	 */
	private void setupWorldGuard() {
		Plugin wg = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (OtherBlocks.worldguardPlugin == null) {
			OtherBlocks.logInfo("Couldn't load WorldGuard.");
		} else {
			OtherBlocks.worldguardPlugin = (WorldGuardPlugin)wg;
			OtherBlocks.logInfo("Hooked into WorldGuard.");			
		}
	}

	public OtherBlocks() {

		blockListener = new ObBlockListener(this);
		entityListener = new ObEntityListener(this);
		vehicleListener = new ObVehicleListener(this);
		playerListener = new ObPlayerListener(this);
		serverListener = new ObServerListener(this);
		
		// this list is used to store the last thing to damage another entity
		damagerList = new HashMap<Entity, Agent>();
		
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
		
		plugin = this;
		getDataFolder().mkdirs();

		//setupPermissions();
		config = new OtherBlocksConfig(this);
		config.load();
		setupWorldGuard();

		// Register events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);

		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, config.pri, this);
		pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, config.pri, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, config.pri, this);
		pm.registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, config.pri, this);
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

		bigBrother = (BigBrother) pm.getPlugin("BigBrother");
		
		logInfo("("+this.getDescription().getVersion()+") loaded.");
	}

	// If logblock plugin is available, inform it of the block destruction before we change it
	public boolean queueBlockBreak(String playerName, Block block)
	{
		String message = playerName+"-broke-"+block.getType().toString();
		
		if (bigBrother != null) {
			// Block Breakage
			OtherBlocks.logInfo("Attempting to log to BigBrother: "+message, 4);
			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
		}
		
		if (lbconsumer != null) {
			BlockState before = block.getState();
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
	
	/**
	 * Matches an actual drop against the configuration and runs any configured drops that are found.
	 * @param drop The actual drop.
	 */
	public void performDrop(OccurredDrop drop) {
		DropsList drops = config.blocksHash.getList(drop.getAction(), drop.getTarget());
		if (drops == null) return;  // TODO: if no drops, just return - is this right?
		if(drop.getTarget() instanceof BlockTarget) {
			Block block = drop.getLocation().getBlock();
			String name = "(unknown)";
			if(drop.getTool() instanceof PlayerSubject)
				name = ((PlayerSubject)drop.getTool()).getPlayer().getName();
			queueBlockBreak(name, block);
		}
		Set<String> exclusives = new HashSet<String>();
		for(CustomDrop match : drops.list) {
			if(!match.matches(drop)) continue;
			if(match.willDrop(exclusives)) {
				match.perform(drop);
				drop.setCancelled(true);
			}
		}
	}
	
	public void startProfiling(String event) {
		if(!OtherBlocks.plugin.config.profiling) return;
		if(!profileMap.containsKey(event)) profileMap.put(event, new ProfilerEntry());
		ProfilerEntry entry = profileMap.get(event);
		if(entry.profiling) // Shouldn't happen, I think
			OtherBlocks.logWarning("Sync error, already profiling for " + event + "!");
		entry.profiling = true;
		entry.started = System.currentTimeMillis();
	}

	public void stopProfiling(String event) {
		if(!OtherBlocks.plugin.config.profiling) return;
		ProfilerEntry entry = profileMap.get(event);
		if(entry.profiling) { // Shouldn't happen, I think
			OtherBlocks.logWarning("Sync error, not profiling for " + event + "!");
			return;
		}
		long endTime = System.currentTimeMillis();
		OtherBlocks.logInfo("SimpleDrop.run() took " + (endTime - entry.started) + " milliseconds.",4);
		entry.list.add(endTime - entry.started);
		entry.profiling = false;
		entry.started = 0;
	}
	
	public void clearProfiling() {
		for(ProfilerEntry entry : profileMap.values())
			entry.list.clear();
	}

	public List<Long> getProfiling(String event) {
		if(!profileMap.containsKey(event)) return null;
		return profileMap.get(event).list;
	}
	
	private static class ProfilerEntry {
		public long started;
		public boolean profiling;
		public List<Long> list = new ArrayList<Long>();
	}
}
