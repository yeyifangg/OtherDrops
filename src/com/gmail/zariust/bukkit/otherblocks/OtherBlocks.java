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

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

//import org.bukkit.*;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.TreeType;

import com.gmail.zariust.bukkit.common.*;
import com.gmail.zariust.register.payment.Method;
import com.gmail.zariust.register.payment.Method.MethodAccount;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
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
	protected Priority pri;
	protected Integer verbosity;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;

	// Listeners
	private final OtherBlocksBlockListener blockListener;
	private final OtherBlocksEntityListener entityListener;
	private final OtherBlocksVehicleListener vehicleListener;

	// for Register (economy support)
	public static Method Method = null;

	// for LogBlock support
	public static Consumer lbconsumer = null;

	// for Permissions support
	public PermissionHandler permissionHandler = null;
	public static Plugin permissionsPlugin;
	public static PermissionHandler worldguardHandler;
	String permiss;
	public boolean usePermissions;

	// for WorldGuard support
	public static Plugin worldguardPlugin;

	// LogInfo & Logwarning - display messages with a standard prefix
	void logWarning(String msg) {
		log.warning("["+getDescription().getName()+"] "+msg);		
	}
	void logInfo(String msg) {
		log.info("["+getDescription().getName()+"] "+msg);
	}

	// LogInfo & LogWarning - if given a level will report the message
	// only for that level & above
	void logInfo(String msg, Integer level) {
		if (config.verbosity >= level) logInfo(msg);
	}
	void logWarning(String msg, Integer level) {
		if (config.verbosity >= level) logWarning(msg);
	}

	// Setup access to the permissions plugin if enabled in our config file
	// TODO: would be simple to create a dummy permissions class (returns true for all has() and false for ingroup()) so we don't need to 
	// keep checking if permissions is null
	void setupPermissions() {
		permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

		if (usePermissions) {
			if (this.permissionHandler == null) {
				if (permissionsPlugin != null) {
					this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					if (this.permissionHandler != null) {
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

	// Setup WorldGuardAPI
	// TODO: work out how to tap into the region name, ie. check if a block is in a particular named region
	private void setupWorldGuard() {
		worldguardPlugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (this.worldguardHandler == null) {
			if (worldguardPlugin != null) {
				this.worldguardHandler = ((Permissions) worldguardPlugin).getHandler();
				System.out.println("[OtherBlocks] hooked into Permissions.");
				permiss = "Yes";
			} else {
				// TODO: read ops.txt file if Permissions isn't found.
				System.out.println("[OtherBlocks] Permissions not found.  Permissions disabled.");
				permiss = "No";
			}
		}
	}

	public OtherBlocks() {

		blockListener = new OtherBlocksBlockListener(this);
		entityListener = new OtherBlocksEntityListener(this);
		vehicleListener = new OtherBlocksVehicleListener(this);

		damagerList = new HashMap<Entity, String>();
		rng = new Random();
		log = Logger.getLogger("Minecraft");

		verbosity = 2;
		pri = Priority.Lowest;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!label.equalsIgnoreCase("otherblocksreload") && !label.equalsIgnoreCase("obr")) return false;

		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (permissionHandler != null) {
				if (permissionHandler.has(player, "otherblocks.admin.reloadconfig")) {
					config.reload();
					player.sendMessage("Otherblocks config reloaded  (Permissions enabled)");
				} else {
					player.sendMessage("You don't have permission for that command.");
				}
			} else {
				// No Permissions - just use ops?
				if (player.isOp()) {
					config.reload();
					player.sendMessage("Otherblocks config reloaded (Permissions disabled)");					
				} else {
					player.sendMessage("You don't have permission for that command  (permissions disabled - ops only).");
				}
			}
		} else {
			config.reload();
		}

		return true;
	}


	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		getDataFolder().mkdirs();


		//setupPermissions();
		//setupWorldGuard();

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

		// BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
		if (this.enableBlockTo) {
			pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, pri, this); //*
		}

		// Register logblock plugin so that we can send break event notices to it
		final Plugin logBlockPlugin = pm.getPlugin("LogBlock");
		if (logBlockPlugin != null)
			lbconsumer = ((LogBlock)logBlockPlugin).getConsumer();

		config = new OtherBlocksConfig(this);
		config.load();
		logInfo("("+this.getDescription().getVersion()+") loaded.");
	}

	// If logblock plugin is available, inform it of the block destruction before we change it
	public static boolean queueBlockBreak(java.lang.String playerName, org.bukkit.block.BlockState before)
	{
		if (lbconsumer != null) {
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

	protected static void setDataValues(OB_Drop obc, String dataString, String objectString, Boolean dropData) {
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

	protected static void performDrop(Location target, OB_Drop dropData, Player player) {

		// Events
		Location treeLocation = target;
		if (!isCreature(dropData.dropped)) {
			treeLocation.setY(treeLocation.getY()+1);
		}
		for(String events : dropData.event) {
			if(events != null) {
				if(events.equalsIgnoreCase("EXPLOSION")) {
					//log.info("explosion!");
					target.getWorld().createExplosion(target, 4);
				} else if(events.equalsIgnoreCase("TREE") || events.equalsIgnoreCase("TREE@GENERIC")) {
					//log.info("tree!"+target.getWorld().getName());
					target.getWorld().generateTree(treeLocation, TreeType.TREE);
					// TODO: refactor - yes, I know this is lazy coding :D  It's late and want to release.
				} else if(events.equalsIgnoreCase("TREE@BIG_TREE")) {
					//log.info("tree!"+target.getWorld().getName());
					target.getWorld().generateTree(treeLocation, TreeType.BIG_TREE);
				} else if(events.equalsIgnoreCase("TREE@BIRCH")) {
					//log.info("tree!"+target.getWorld().getName());
					target.getWorld().generateTree(treeLocation, TreeType.BIRCH);
				} else if(events.equalsIgnoreCase("TREE@REDWOOD")) {
					//log.info("tree!"+target.getWorld().getName());
					target.getWorld().generateTree(treeLocation, TreeType.REDWOOD);
				} else if(events.equalsIgnoreCase("TREE@TALL_REDWOOD")) {
					//log.info("tree!"+target.getWorld().getName());
					target.getWorld().generateTree(treeLocation, TreeType.TALL_REDWOOD);
				} else if(events.equalsIgnoreCase("LIGHTNING")) {
					target.getWorld().strikeLightning(target);
				} else if(events.equalsIgnoreCase("LIGHTNING@HARMLESS")) {
					target.getWorld().strikeLightningEffect(target);
				}
			}
		}

		// Do actual drop

		String amountString = "unknown";

		// **************
		// DROP money
		// **************
		if (dropData.dropped.equalsIgnoreCase("MONEY"))
		{
			if (player != null) {
				if (Method.hasAccount(player.getName()))
				{
					MethodAccount account = Method.getAccount(player.getName());
					Double amount = Double.valueOf(dropData.getRandomQuantityDouble()); 
					account.add(amount);
					amountString = amount.toString();
				}
			}
		// **************
		// DROP blocks
		// **************
		} else if(!isCreature(dropData.dropped)) {
			if(!dropData.dropped.equalsIgnoreCase("DEFAULT")) { 
				if(dropData.dropped.equalsIgnoreCase("CONTENTS")) {
					doContentsDrop(target, dropData);
				} else { // Material should be valid - check for int value first, otherwise get material by string name
					Material dropMaterial = null;
					try {
						Integer originalInt = Integer.valueOf(dropData.dropped);
						dropMaterial = Material.getMaterial(originalInt);
					} catch(NumberFormatException x) {
						dropMaterial = Material.valueOf(dropData.dropped.toUpperCase());
					}
					// Special exemption for AIR - breaks the map! :-/
					if(dropMaterial != Material.AIR) {
						Integer amount = dropData.getRandomQuantityInt();
						amountString = amount.toString();
						if (amount != 0) { // 0 causes an "infitite" block that fills your inventory but can't be built)
							Short dropDataColor = dropData.getRandomDropData();
							if (dropDataColor == null) dropDataColor = 0;
							target.getWorld().dropItemNaturally(target, new ItemStack(dropMaterial, amount, dropDataColor));
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
				Entity critter = target.getWorld().spawnCreature(
						new Location(target.getWorld(), target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5), 
						CreatureType.valueOf(OtherBlocks.creatureName(dropData.dropped))
				);
				String critterTypeName = CreatureType.valueOf(OtherBlocks.creatureName(dropData.dropped)).toString();
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
				} else if(critter instanceof Colorable) {
					Colorable ccrit = (Colorable)critter;
					ccrit.setColor(DyeColor.getByData(dataVal.byteValue()));
				}


			}
		}

		// Show player (if any) a message (if set)
		sendPlayerRandomMessage(player, dropData.messages, amountString);
	}

	static void sendPlayerRandomMessage(Player player, List<String> messages, String amountString)
	{
		try {
			if (player != null) {
				if (messages != null) {
					// TOFIX:: not recommended to run two random number generators?  better way of selecting random message?
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

	private static void doContentsDrop(Location target, OB_Drop dropData) {

		List<ItemStack> drops = new ArrayList<ItemStack>();
		Inventory inven = null;

		switch(Material.valueOf(dropData.original.toUpperCase())) {
		case FURNACE:
		case BURNING_FURNACE:
			Furnace oven = (Furnace) target.getBlock().getState();
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

		if(drops.size() > 0) {
			for(ItemStack item : drops) {
				if(item.getType() != Material.AIR) {
					target.getWorld().dropItemNaturally(target, item);
				}
			}
		}
	}
}
