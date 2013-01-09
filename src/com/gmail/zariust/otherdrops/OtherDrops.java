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

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import me.drakespirit.plugins.moneydrop.MoneyDrop;
import me.taylorkelly.bigbrother.BigBrother;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import uk.co.oliwali.HawkEye.DataType;
import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.entry.BlockEntry;
import uk.co.oliwali.HawkEye.entry.DataEntry;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

import com.garbagemule.MobArena.MobArenaHandler;
import com.gmail.zariust.common.Verbosity;
import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.metrics.Metrics;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.DropRunner;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.GroupDropEvent;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.listener.*;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Subject.ItemCategory;
import com.gmail.zariust.register.payment.Method;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class OtherDrops extends JavaPlugin
{
	public static OtherDrops plugin;
	public PluginDescriptionFile info = null;
	static String pluginName;
	static String pluginVersion;
	public static Profiler profiler;
	static Logger log = Logger.getLogger("Minecraft");

	// Global random number generator - used throughout the whole plugin
	public static Random rng = new Random(); 

	// Config stuff
	public OtherDropsConfig config = null;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;

	// Listeners
	final OdBlockListener blockListener;
	final OdEntityListener entityListener;
	final OdVehicleListener vehicleListener;
	final OdPlayerListener playerListener;
	final OdServerListener serverListener;

	// Plugin Dependencies
	public static Method method = null;      						// for Register (economy support)
	public static Consumer lbconsumer = null; 						// for LogBlock support
	public static BigBrother bigBrother = null;						// for BigBrother support
	public static PermissionHandler yetiPermissionsHandler = null;	// for Permissions support
	public static WorldGuardPlugin worldguardPlugin = null;			// for WorldGuard support
    public boolean usingHawkEye = false; 							// for HawkEye support
	boolean enabled;
	public static MobArenaHandler mobArenaHandler = null;			// for MobArena
	public static MoneyDrop moneyDropHandler;						// for MoneyDrop
	
    public static Economy vaultEcon = null;
    public static Permission vaultPerms = null;

	public OtherDrops() {
		plugin = this;
		
		blockListener = new OdBlockListener(this);
		entityListener = new OdEntityListener(this);
		vehicleListener = new OdVehicleListener(this);
		playerListener = new OdPlayerListener(this);
		serverListener = new OdServerListener(this);
		
		profiler = new Profiler();
				
	}

	@Override
	public void onEnable()
	{
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
		
		// Set plugin name & version, this must be at the start of onEnable
		// Used in log messages throughout
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
	
		// Create the data folder (if not there already) and load the config
		getDataFolder().mkdirs();
		config = new OtherDropsConfig(this);
		config.load();
		
		setupPluginDependencies();

		enableOtherDrops(); // register events
		
		this.getCommand("od").setExecutor(new OtherDropsCommand(this));
	
		// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
		if (this.enableBlockTo) {
			//pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, config.priority, this);
		}
			
		Log.logInfo("OtherDrops loaded.");
	}

	@Override
	public void onDisable() { log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded."); }

	private void setupPluginDependencies() {
		try {
			setupWorldGuard();
		} catch (Exception e) {
			Log.logInfo("Failed to load WorldGuard (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupMobArena();
		} catch (Exception e) {
			Log.logInfo("Failed to load MobArena (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupMoneyDrop();
		} catch (Exception e) {
			Log.logInfo("Failed to load MoneyDrop (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupHawkEye();
		} catch (Exception e) {
			Log.logInfo("Failed to load HawkEye (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupLogBlock();
		} catch (Exception e) {
			Log.logInfo("Failed to load LogBlock (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupBigBrother();
		} catch (Exception e) {
			Log.logInfo("Failed to load BigBrother (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			setupVault();
		} catch (Exception e) {
			Log.logInfo("Failed to load Vault (something went wrong) - continuing OtherDrops startup.");
			e.printStackTrace();
		}
	}

	private void setupLogBlock() {
		// Register logblock plugin so that we can send break event notices to it
		final Plugin logBlockPlugin = getServer().getPluginManager().getPlugin("LogBlock");
		if (logBlockPlugin != null) {
			lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();
	        Log.logInfo("Hooked into LogBlock.", HIGH);
		}
	}

	private void setupBigBrother() {
		bigBrother = (BigBrother) getServer().getPluginManager().getPlugin("BigBrother");
	}

	private void setupHawkEye() {
		// Check for HawkEye plugin
	    Plugin dl = getServer().getPluginManager().getPlugin("HawkEye");
	    if (dl != null) {
	    	//hawkeyePlugin = (HawkEye)dl;
	        this.usingHawkEye = true;
	        Log.logInfo("Hooked into HawkEye.");
	    }
	}

	// Setup access to the permissions plugin if enabled in our config file
	void setupPermissions(boolean useYeti) {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		if (useYeti) {
			if (OtherDrops.yetiPermissionsHandler == null) {
				if (permissionsPlugin != null) {
					OtherDrops.yetiPermissionsHandler = ((Permissions) permissionsPlugin).getHandler();
					if (OtherDrops.yetiPermissionsHandler != null) {
						Log.logInfo("Hooked into YetiPermissions.", Verbosity.HIGH);
					} else {
						Log.logInfo("Cannot hook into YetiPermissions - failed.", Verbosity.NORMAL);
					}
				} else {
					Log.logInfo("YetiPermissions not found.", Verbosity.NORMAL);
				}
			}
		} else {
			Log.logInfo("YetiPermissions (useyetipermissions) not enabled in config.", Verbosity.HIGH);
			yetiPermissionsHandler = null;
		}
		if(yetiPermissionsHandler == null) Log.logInfo("Using Bukkit superperms.", Verbosity.NORMAL);
	}

	/**
	 * Setup WorldGuardAPI - hook into the plugin if it's available
	 */
	private void setupWorldGuard() {
		Plugin wg = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (wg == null) {
			Log.logInfo("Couldn't load WorldGuard.", Verbosity.NORMAL);
		} else {
			OtherDrops.worldguardPlugin = (WorldGuardPlugin)wg;
			Log.logInfo("Hooked into WorldGuard.", Verbosity.HIGH);			
		}
	}
	
	private void setupMobArena() {
		Plugin ma = this.getServer().getPluginManager().getPlugin("MobArena");
		if (ma == null) {
			Log.logInfo("Couldn't load MobArena.",EXTREME); // mobarena's not essential so no need to worry.
			mobArenaHandler = null;
		} else {
			Log.logInfo("Hooked into MobArena.",HIGH);
			mobArenaHandler = new MobArenaHandler();
		}		
	}

	private void setupMoneyDrop() {
		Plugin plug = this.getServer().getPluginManager().getPlugin("MoneyDrop");
		if (plug == null) {
			Log.logInfo("Couldn't load MoneyDrop.",EXTREME); // MoneyDrop's not essential so no need to worry.
			moneyDropHandler = null;
		} else {
			moneyDropHandler = (me.drakespirit.plugins.moneydrop.MoneyDrop)plug;			
			Log.logInfo("Hooked into MoneyDrop.",HIGH);
		}
		
	}
    
	private void setupVault() {
	        if (getServer().getPluginManager().getPlugin("Vault") == null) {
	            vaultEcon = null;
				Log.logInfo("Couldn't load Vault.",EXTREME); // Vault's not essential so no need to worry.
				return;
	        }
			Log.logInfo("Hooked into Vault.",HIGH);
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
	            vaultEcon = null;
				Log.logWarning("...couldn't hook into Vault economy module.",Verbosity.NORMAL);
	            return;
	        }
	        vaultEcon = rsp.getProvider();

	    //   RegistereredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
	    //   chat = rsp.getProvider();
	    //    return chat != null;

	        RegisteredServiceProvider<Permission> rsp_perms = getServer().getServicesManager().getRegistration(Permission.class);
	        if (rsp == null) {
	            vaultPerms = null;
				Log.logWarning("...couldn't hook into Vault permissions module.",Verbosity.NORMAL);
	            return;
	        }
	        vaultPerms = rsp_perms.getProvider();
	}
	
	// If logblock plugin is available, inform it of the block destruction before we change it
	public boolean queueBlockBreak(String playerName, Block block)
	{
		if (block == null) {
			Log.logWarning("Queueblockbreak: block is null - this shouldn't happen (please advise developer).  Player = "+playerName, HIGH);			
			return false;
		}
		
		String message = playerName+"-broke-"+block.getType().toString();
		
		if (bigBrother != null) {
			// Block Breakage
			Log.logInfo("Attempting to log to BigBrother: "+message, HIGHEST);
			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
		}
		
		if (lbconsumer != null) {
			BlockState before = block.getState();
			Log.logInfo("Attempting to log to LogBlock: "+message, HIGHEST);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		
		if (this.usingHawkEye == true) {
			Log.logInfo("Attempting to log to HawkEye: "+message, HIGHEST);
			
			// FIXME: Causes class not found since I'm using "new BlockEntry(...)" - need to stick to API methods?
//			boolean result = HawkEyeAPI.addEntry(plugin, new BlockEntry(playerName, DataType.BLOCK_BREAK, block));

			boolean result = HawkEyeAPI.addCustomEntry(this, "ODBlockBreak", getServer().getPlayer(playerName), block.getLocation(), block.getType().toString());
			if (!result) Log.logWarning("Warning: HawkEyeAPI logging failed.", Verbosity.HIGH);
		}
		return true;
	}
	
	/**
	 * Matches an actual drop against the configuration and runs any configured drops that are found.
	 * @param occurence The actual drop.
	 */
	public void performDrop(OccurredEvent occurence) {
		DropsList customDrops = config.blocksHash.getList(occurence.getAction(), occurence.getTarget());
		if (customDrops == null) {
			Log.logInfo("PerformDrop ("+(occurence.getAction()==null ? "":occurence.getAction().toString())+", "+(occurence.getTarget()==null ? "":occurence.getTarget().toString())+" w/ "+(occurence.getTool()==null ? "":occurence.getTool().toString())+") no potential drops found", HIGHEST);
			return;  // TODO: if no drops, just return - is this right?
		}
		// TODO: return a list of drops found? difficult due to multi-classes?
		Log.logInfo("PerformDrop - potential drops found: "+customDrops.toString() + " tool: "+(occurence.getTool()==null ? "":occurence.getTool().toString()), HIGH);
		
		// check if block is excepted (for any)
		for (CustomDrop drop : customDrops) {
			if (drop.getTarget() instanceof BlockTarget) {
				BlockTarget any = (BlockTarget)drop.getTarget();
				if (any.except != null) {
					Material compareTo = null;
					if (occurence.getEvent() instanceof BlockBreakEvent) {
						compareTo = ((BlockBreakEvent)occurence.getEvent()).getBlock().getType();
					} else if (occurence.getEvent() instanceof PlayerInteractEvent) {
						compareTo = null;
						PlayerInteractEvent pie = (PlayerInteractEvent)occurence.getEvent();
						if (pie.getPlayer() != null) {
							compareTo = pie.getPlayer().getItemInHand().getType();
						}
					}
					
					if (any.except.contains(compareTo)) {
						return;
					}
				}
			}
		}

		// Loop through the drops and check for a match, process uniques, etc	
		List<SimpleDrop> scheduledDrops = gatherDrops(customDrops, occurence);
		Log.logInfo("PerformDrop: scheduled drops="+scheduledDrops.toString(), HIGHEST);

		// check for any DEFAULT drops
		boolean defaultDrop = false;
		int dropCount = 0;
		for (SimpleDrop simpleDrop : scheduledDrops) {
		    if (simpleDrop.getDropped() != null)
		    	//if (!simpleDrop.getDropped().toString().equalsIgnoreCase("AIR")) // skip drops that don't actually drop anything
		    		dropCount++;
		    if (simpleDrop.isDefault()) {
		    	defaultDrop = true;
		    	occurence.setOverrideDefault(false); // DEFAULT drop
		    }
		    if (simpleDrop.getDropped() != null && simpleDrop.getDropped().toString().equalsIgnoreCase("AIR")) occurence.setOverrideDefault(true); // NOTHING drop
		}	


		for (SimpleDrop simpleDrop : scheduledDrops) {
			Log.logInfo("PerformDrop: scheduling " + simpleDrop.getDropName(), HIGH);
			scheduleDrop(occurence, simpleDrop, defaultDrop);
		}
		
		// Cancel event, if applicable
		if (occurence.isOverrideDefault() && !defaultDrop) {
			if (occurence.getEvent() instanceof BlockBreakEvent || occurence.getEvent() instanceof PlayerFishEvent) {
				if (occurence.getTool().getType() != ItemCategory.EXPLOSION) {
				Log.logInfo("PerformDrop: blockbreak or fishing - not default drop - cancelling event (dropcount="+dropCount+").", HIGH);
				occurence.setCancelled(true);

				// Process action through logging plugins, if any - this is only because we generally cancel the break event
				if(occurence.getTarget() instanceof BlockTarget && occurence.getAction() == Action.BREAK) {
					Block block = occurence.getLocation().getBlock();
					String playerName = "(unknown)";
					if(occurence.getTool() instanceof PlayerSubject)
						playerName = ((PlayerSubject)occurence.getTool()).getPlayer().getName();
					queueBlockBreak(playerName, block);
				}
				}
			} else if (occurence.getRealEvent() != null) {
				if (occurence.getRealEvent() instanceof EntityDeathEvent) {
					EntityDeathEvent evt = (EntityDeathEvent) occurence.getRealEvent();
					if ((evt.getEntity() instanceof Player) && !(occurence.isDenied())) {
						Log.logInfo("Player death - not clearing.");
					} else {
						Log.logInfo("PerformDrop: entitydeath - clearing drops.", HIGHEST);
						evt.getDrops().clear();
						// and if denied just remove the entity to stop animation (as we cannot cancel the event)
						if (occurence.isDenied()) {
							evt.getEntity().remove();
						}
					}
					if (OtherDropsConfig.disableXpOnNonDefault) {
						Log.logInfo("PerformDrop: entitydeath - no default drop, clearing xp drop.", HIGH);
						evt.setDroppedExp(0);
					}
				}
			}
		}
		if (occurence.getRealEvent() != null) {
			if (occurence.getRealEvent() instanceof EntityDeathEvent) {
				EntityDeathEvent evt = (EntityDeathEvent) occurence.getRealEvent();
				if (occurence.isOverrideDefaultXp()) {
					Log.logInfo("PerformDrop: entitydeath - isOverrideDefaultXP=true, clearing xp drop.", HIGH);
					evt.setDroppedExp(0);
				}
			}
		}
		
		
		
		
		// Make sure explosion events are not cancelled (as this will cancel the whole explosion
		// Individual blocks are prevented (if DENY is set) in the entity listener
		if (occurence.getEvent() instanceof EntityExplodeEvent) occurence.setCancelled(false); 
		Log.logInfo("PerformDrop: finished. defaultdrop="+defaultDrop+" dropcount="+dropCount+" cancelled="+occurence.isCancelled()+" denied="+occurence.isDenied(), HIGH);					
	}
	

	private List<SimpleDrop> gatherDrops (DropsList customDrops, OccurredEvent occurence) {
//		OtherDrops.logInfo("Gatherdrops start.", HIGHEST);

		List<CustomDrop> matchedDrops = new ArrayList<CustomDrop>(); // rename to matchedDrops
		List<CustomDrop> uniqueList = new ArrayList<CustomDrop>();

		// First, loop through all drops and gather successful & unique ones into two lists
		// Note: since we don't know if this drop will be cleared by uniques, don't do any events in here
		for(CustomDrop customDrop : customDrops) {
			if (customDrop instanceof GroupDropEvent) {
				GroupDropEvent groupCustomDrop = (GroupDropEvent)customDrop;
				if(groupCustomDrop.matches(occurence)) { // FIXME: include chance check at top of matches                        
					//OtherDrops.logInfo("PerformDrop: found group ("+groupCustomDrop.getGroupsString()+")", HIGHEST);
					matchedDrops.add(groupCustomDrop);
					if(!groupCustomDrop.getFlagState().continueDropping) {  // This means a unique flag found
						//OtherDrops.logInfo("PerformDrop: group ("+groupCustomDrop.getName()+") is UNIQUE.", HIGHEST);
						uniqueList.add(groupCustomDrop);
					}

				} else {
					//OtherDrops.logInfo("PerformDrop: Dropgroup ("+groupCustomDrop.getLogMessage()+") did not match ("+occurence.getLogMessage()+").", HIGHEST);
					continue;
				}
			} else { // SimpleDrop - so add to a list
				if(customDrop.matches(occurence)) {
					matchedDrops.add(customDrop);
					if(!customDrop.getFlagState().continueDropping) {  // This means a unique flag found
						uniqueList.add(customDrop);
					}
				} else {
					//OtherDrops.logInfo("PerformDrop: Drop ("+occurence.getLogMessage()+") did not match ("+customDrop.getLogMessage()+").", HIGHEST);
				}
			}
		}

		// If there were unique, pick a random one and clear the rest
		if (!uniqueList.isEmpty()) {
			matchedDrops.clear();
			matchedDrops.add((CustomDrop)getSingleRandomUnique(uniqueList));
		}


		// Loop through what's left and check for groups that need to be recursed into, otherwise add to final list and return
		List<SimpleDrop> finalDrops = new ArrayList<SimpleDrop>();
		for(CustomDrop customDrop : matchedDrops) {
			if (customDrop instanceof GroupDropEvent) {
				GroupDropEvent groupCustomDrop = (GroupDropEvent)customDrop;
				// Process dropGroup events here...
				// Display dropgroup "message:"
				String message = MessageAction.getRandomMessage(customDrop, occurence, customDrop.getMessages());
				if (message != null && (occurence.getTool() instanceof PlayerSubject)) {
					((PlayerSubject)occurence.getTool()).getPlayer().sendMessage(message);
				}

				finalDrops.addAll(gatherDrops(groupCustomDrop.getDrops(), occurence));
			} else {
				//OtherDrops.logInfo("PerformDrop: adding " + customDrop.getDropName(), HIGHEST);
				finalDrops.add((SimpleDrop)customDrop);
			}
		}

		//OtherDrops.logInfo("Gatherdrops end... finaldrops: "+finalDrops.toString(), HIGHEST);
	      return finalDrops;

	 }
	      

	public CustomDrop getSingleRandomUnique (List<CustomDrop> uniqueList) {
	  CustomDrop random = uniqueList.get(rng.nextInt(uniqueList.size()));
	  Log.logInfo("PerformDrop: getunique, selecting: " + random.getDropName(), HIGHEST);
	  return random;
	}
	
	public void scheduleDrop(OccurredEvent evt, CustomDrop customDrop, boolean defaultDrop) {

		int schedule = customDrop.getRandomDelay();
//		if(schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, this, schedule);
	//	else run();
		
		
        Location playerLoc = null;
        Player player = null; // FIXME: need to get player early - in event
        //if (evt.player != null) playerLoc = player.getLocation();
        DropRunner dropRunner = new DropRunner(OtherDrops.plugin, evt, customDrop, player, playerLoc, defaultDrop);
        
        // schedule the task - NOTE: this must be a sync task due to the changes made in the performActualDrop function
		if(schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, dropRunner, schedule);
		else dropRunner.run();
        //}
	}
	
	public boolean hasPermission(Permissible who, String permission) {
		if (who instanceof ConsoleCommandSender) return true;
		if (yetiPermissionsHandler == null) {
			boolean perm = who.hasPermission(permission);
			if (!perm) {
				Log.logInfo("SuperPerms - permission ("+permission+") denied for "+who.toString(),HIGHEST);
			} else {
				Log.logInfo("SuperPerms - permission ("+permission+") allowed for "+who.toString(),HIGHEST);
			}
			return perm;
		} else {
			if(who instanceof Player) {
				boolean perm = yetiPermissionsHandler.has((Player) who, permission);
				if (!perm) Log.logInfo("Yetiperms - permission ("+permission+") denied for "+who.toString(),HIGHEST);
				return perm;
			} else {
				return who.isOp();
			}
		}
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

	public static boolean inGroup(Player agent, String group) {
		if(yetiPermissionsHandler != null)
			return yetiPermissionsHandler.inGroup(agent.getWorld().getName(), agent.getName(), group);
		return agent.hasPermission("group." + group) || agent.hasPermission("groups." + group);
	}
	
	// TODO: This is only for temporary debug purposes.
	public static void stackTrace() {
		if(plugin.config.verbosity.exceeds(EXTREME)) Thread.dumpStack();
	}

	public void enableOtherDrops() {
		PluginManager pm = Bukkit.getServer().getPluginManager();			
		pm.registerEvents(plugin.serverListener, plugin);
		pm.registerEvents(plugin.blockListener, plugin);
		pm.registerEvents(plugin.entityListener, plugin);
		pm.registerEvents(plugin.vehicleListener, plugin);
		pm.registerEvents(plugin.playerListener, plugin);
		this.enabled = true;
	}

	public void disableOtherDrops() {
		HandlerList.unregisterAll(plugin);		
		this.enabled = false;
	}
}
