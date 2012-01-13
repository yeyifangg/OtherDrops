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

import java.util.*;
import java.util.logging.Logger;

import me.drakespirit.plugins.moneydrop.MoneyDrop;
import me.taylorkelly.bigbrother.BigBrother;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

import com.garbagemule.MobArena.MobArenaHandler;
import com.gmail.zariust.common.Verbosity;
import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.otherdrops.event.CustomDropEvent;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.ExclusiveMap;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.listener.*;
import com.gmail.zariust.otherdrops.options.Flag;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.register.payment.Method;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class OtherDrops extends JavaPlugin
{
	public PluginDescriptionFile info = null;

	private static Logger log;

	// Config stuff
	public OtherDropsConfig config = null;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;

	// Listeners
	private final OdBlockListener blockListener;
	private final OdEntityListener entityListener;
	private final OdVehicleListener vehicleListener;
	private final OdPlayerListener playerListener;
	private final OdServerListener serverListener;

	public static Random rng = new Random();

	// for Register (economy support)
	public static Method method = null;

	// for LogBlock support
	public static Consumer lbconsumer = null;
	
	// for BigBrother support
	public static BigBrother bigBrother = null;

	// for Permissions support
	public static PermissionHandler yetiPermissionsHandler = null;

	// for WorldGuard support
	public static WorldGuardPlugin worldguardPlugin = null;

    public boolean usingHawkEye = false;
        
	// for MobArena
	public static MobArenaHandler mobArenaHandler = null;

	public static MoneyDrop moneyDropHandler;

	private static String pluginName;
	private static String pluginVersion;
	public static OtherDrops plugin;
	public static Profiler profiler;
	
	// LogInfo & Logwarning - display messages with a standard prefix
	public static void logWarning(String msg) {
		log.warning("["+pluginName+":"+pluginVersion+"] "+msg);
	}
	public static void logInfo(String msg) {
		log.info("["+pluginName+":"+pluginVersion+"] "+msg);
	}

	// LogInfo & LogWarning - if given a level will report the message
	// only for that level & above
	public static void logInfo(String msg, Verbosity level) {
		if (plugin.config.verbosity.exceeds(level)) logInfo(msg);
	}
	public static void logWarning(String msg, Verbosity level) {
		if (plugin.config.verbosity.exceeds(level)) logWarning(msg);
	}

	// Setup access to the permissions plugin if enabled in our config file
	void setupPermissions(boolean useYeti) {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		if (useYeti) {
			if (OtherDrops.yetiPermissionsHandler == null) {
				if (permissionsPlugin != null) {
					OtherDrops.yetiPermissionsHandler = ((Permissions) permissionsPlugin).getHandler();
					if (OtherDrops.yetiPermissionsHandler != null) {
						logInfo("Hooked into YetiPermissions.", Verbosity.HIGH);
					} else {
						logInfo("Cannot hook into YetiPermissions - failed.", Verbosity.NORMAL);
					}
				} else {
					logInfo("YetiPermissions not found.", Verbosity.NORMAL);
				}
			}
		} else {
			logInfo("YetiPermissions (useyetipermissions) not enabled in config.", Verbosity.HIGH);
			yetiPermissionsHandler = null;
		}
		if(yetiPermissionsHandler == null) logInfo("Using Bukkit superperms.", Verbosity.NORMAL);
	}

	/**
	 * Setup WorldGuardAPI - hook into the plugin if it's available
	 */
	private void setupWorldGuard() {
		Plugin wg = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (wg == null) {
			OtherDrops.logInfo("Couldn't load WorldGuard.", Verbosity.NORMAL);
		} else {
			OtherDrops.worldguardPlugin = (WorldGuardPlugin)wg;
			OtherDrops.logInfo("Hooked into WorldGuard.", Verbosity.HIGH);			
		}
	}
	
	private void setupMobArena() {
		Plugin ma = this.getServer().getPluginManager().getPlugin("MobArena");
		if (ma == null) {
			OtherDrops.logInfo("Couldn't load MobArena.",EXTREME); // mobarena's not essential so no need to worry.
			mobArenaHandler = null;
		} else {
			OtherDrops.logInfo("Hooked into MobArena.",HIGH);
			mobArenaHandler = new MobArenaHandler();
			Flag.register(new Flag(ma, "IN_MOB_ARENA") {
				@Override public void matches(OccurredDropEvent event, boolean state, final FlagState result) {
					if (OtherDrops.mobArenaHandler == null) {
						result.dropThis = true;
					} else {
						if(state) result.dropThis = true;
						else if(mobArenaHandler.inRunningRegion(event.getLocation()))
							result.dropThis = false;
						else result.dropThis = true;
					}
				}
			});
		}
		
	}

	private void setupMoneyDrop() {
		Plugin plug = this.getServer().getPluginManager().getPlugin("MoneyDrop");
		if (plug == null) {
			OtherDrops.logInfo("Couldn't load MobArena.",EXTREME); // mobarena's not essential so no need to worry.
			moneyDropHandler = null;
		} else {
			moneyDropHandler = (me.drakespirit.plugins.moneydrop.MoneyDrop)plug;			
			OtherDrops.logInfo("Hooked into MoneyDrop.",HIGH);
		}
		
	}

	public OtherDrops() {
		plugin = this;
		
		blockListener = new OdBlockListener(this);
		entityListener = new OdEntityListener(this);
		vehicleListener = new OdVehicleListener(this);
		playerListener = new OdPlayerListener(this);
		serverListener = new OdServerListener(this);
		
		profiler = new Profiler();
				
		log = Logger.getLogger("Minecraft");
	}

	public boolean hasPermission(Permissible who, String permission) {
		if (who instanceof ConsoleCommandSender) return true;
		if (yetiPermissionsHandler == null) {
			boolean perm = who.hasPermission(permission);
			if (!perm) {
				OtherDrops.logInfo("SuperPerms - permission ("+permission+") denied for "+who.toString(),HIGHEST);
			} else {
				OtherDrops.logInfo("SuperPerms - permission ("+permission+") allowed for "+who.toString(),HIGHEST);
			}
			return perm;
		} else {
			if(who instanceof Player) {
				boolean perm = yetiPermissionsHandler.has((Player) who, permission);
				if (!perm) OtherDrops.logInfo("Yetiperms - permission ("+permission+") denied for "+who.toString(),HIGHEST);
				return perm;
			} else {
				return who.isOp();
			}
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
	
		getDataFolder().mkdirs();
		
		config = new OtherDropsConfig(this);
		config.load();
		setupWorldGuard();
		setupMobArena();
		setupMoneyDrop();
		
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
		
		this.getCommand("od").setExecutor(new OtherDropsCommand(this));

		// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
		if (this.enableBlockTo) {
			pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, config.pri, this);
		}

		// Check for HawkEye plugin
        Plugin dl = getServer().getPluginManager().getPlugin("HawkEye");
        if (dl != null) {
            this.usingHawkEye = true;
            OtherDrops.logInfo("Hooked into HawkEye.");
        }


        
		// Register logblock plugin so that we can send break event notices to it
		final Plugin logBlockPlugin = pm.getPlugin("LogBlock");
		if (logBlockPlugin != null)
			lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();

		bigBrother = (BigBrother) pm.getPlugin("BigBrother");
		
		logInfo("OtherDrops loaded.");
	}

	// If logblock plugin is available, inform it of the block destruction before we change it
	public boolean queueBlockBreak(String playerName, Block block)
	{
		String message = playerName+"-broke-"+block.getType().toString();
		
		if (bigBrother != null) {
			// Block Breakage
			OtherDrops.logInfo("Attempting to log to BigBrother: "+message, HIGHEST);
			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
		}
		
		if (lbconsumer != null) {
			BlockState before = block.getState();
			logInfo("Attempting to log to LogBlock: "+message, HIGHEST);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		
		if (this.usingHawkEye == true) {
	        HawkEyeAPI.addCustomEntry(this, "ODBlockBreak", getServer().getPlayer(playerName), block.getLocation(), block.getType().toString());
	        //HawkEyeAPI.addCustomEntry(this, uk.co.oliwali.HawkEye.DataType.BLOCK_BREAK, getServer().getPlayer(playerName), block.getLocation(), block.getType().toString());
		}
		return true;
	}
	
	public List<String> getGroups(Player player) {
		if(yetiPermissionsHandler != null)
			return Arrays.asList(yetiPermissionsHandler.getGroups(player.getWorld().getName(), player.getName()));
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
	public void performDrop(OccurredDropEvent drop) {
		OtherDrops.logInfo("PerformDrop - checking for potential drops: action = " + drop.getAction() + " target = " + drop.getTarget(), HIGHEST);
		DropsList drops = config.blocksHash.getList(drop.getAction(), drop.getTarget());
		if (drops == null) {
			OtherDrops.logInfo("PerformDrop - no potential drops found", HIGHEST);
			return;  // TODO: if no drops, just return - is this right?
		}
		// TODO: return a list of drops found? difficult due to multi-classes?
		OtherDrops.logInfo("PerformDrop - potential drops found: "+drops.toString() + " tool: "+(drop.getTool()==null ? "":drop.getTool().toString()), HIGH);
		if(drop.getTarget() instanceof BlockTarget) {
			Block block = drop.getLocation().getBlock();
			String name = "(unknown)";
			if(drop.getTool() instanceof PlayerSubject)
				name = ((PlayerSubject)drop.getTool()).getPlayer().getName();
			queueBlockBreak(name, block);
		}
		ExclusiveMap exclusives = new ExclusiveMap(drops,drop);

		// Loop through the drops and check for a match
		boolean defaultDrop = false;
		int dropCount = 0;
		for(CustomDropEvent match : drops) {
			if(!match.matches(drop)) {
				OtherDrops.logInfo("PerformDrop: Drop ("+drop.getLogMessage()+") did not match ("+match.getLogMessage()+").", HIGHEST);
				continue;
			}
			if(match.willDrop(exclusives)) {
				OtherDrops.logInfo("PerformDrop: dropping " + match.getDropName(), HIGH);
				match.perform(drop);
				dropCount++;
				if (match.isDefault()) defaultDrop = true;
			} else {
				OtherDrops.logInfo("PerformDrop: Not dropping - match.willDrop(exclusives) failed.",HIGHEST);
			}
			if(!match.getFlagState().continueDropping) {
				OtherDrops.logInfo("PerformDrop: A flag has aborted the drop processing before considering all possibilities.",HIGHEST);
				break;
			}
		}
		
		// Cancel event, if applicable
		if (!defaultDrop && dropCount > 0) drop.setCancelled(true);
		if (drop.getEvent() instanceof EntityExplodeEvent) drop.setCancelled(false); // TODO: write comment here as to why we don't cancel the explosion
	}
	
	public static boolean inGroup(Player agent, String group) {
		if(yetiPermissionsHandler != null)
			return yetiPermissionsHandler.inGroup(agent.getWorld().getName(), agent.getName(), group);
		return agent.hasPermission("group." + group) || agent.hasPermission("groups." + group);
	}
	
	// TODO: This is only for temporary debug purposes.
	public static void stackTrace() {
		if(plugin.config.verbosity.exceeds(EXTREME)) Thread.dumpStack();
	}
}
