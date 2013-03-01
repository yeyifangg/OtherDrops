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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.drop.DropResult;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.GroupDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.Target;
import com.herocraftonline.heroes.characters.Hero;

public class OtherDropsCommand implements CommandExecutor {
	private enum OBCommand {
		ID("id", "i"),
		RELOAD("reload", "r"),
		SHOW("show", "s"),
		SETTINGS("settings", "st"),
		DISABLE("disable,disabled,off", "o"),
		ENABLE("enable,enabled,on", "e"),
		HEROESTEST("heroestest", "ht"),
		DROP("drop", "d");
		private String cmdName;
		private String cmdShort;

		private OBCommand(String name, String abbr) {
			cmdName = name;
			cmdShort = abbr;
		}
		
		public static OBCommand match(String label, String firstArg) {
			boolean arg = false;
			if(label.equalsIgnoreCase("od")) arg = true;
			for(OBCommand cmd : values()) {
				if(arg) {
					for (String item : cmd.cmdName.split(",")) {
						if (firstArg.equalsIgnoreCase(item)) return cmd;
					}
				}
				else if(label.equalsIgnoreCase("od" + cmd.cmdShort) || label.equalsIgnoreCase("od" + cmd.cmdName))
					return cmd;
			}
			return null;
		}

		public String[] trim(String[] args, StringBuffer name) {
			if(args.length == 0) return args;
			if(!args[0].equalsIgnoreCase(cmdName)) return args;
			String[] newArgs = new String[args.length - 1];
			System.arraycopy(args, 1, newArgs, 0, newArgs.length);
			if(name != null) name.append(" " + args[0]);
			return newArgs;
		}
	}
	private final OtherDrops otherdrops;
	
	public OtherDropsCommand(OtherDrops plugin) {
		otherdrops = plugin;
	}
	
	private String getName(CommandSender sender) {
		if(sender instanceof ConsoleCommandSender) return "CONSOLE";
		else if(sender instanceof Player) return ((Player) sender).getName();
		else return "UNKNOWN";
	}
	
	// returns null if not durability is not valid (ie. Has no “maxdurability”)
	private Double getDurabilityPercentage(ItemStack item) {
		Double maxDura = Double.valueOf(item.getType().getMaxDurability());
		Double dura = Double.valueOf(item.getDurability());
		
		if (maxDura < 1) return null;
		return (1-(dura/maxDura));
	}

	public static Entity getTarget(final Player player) {
		 
        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 100);
        Entity target = null;
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return target = entity;
                            }
            }
        }
        return target;
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		OBCommand cmd = OBCommand.match(label, args.length >= 1 ? args[0] : "");
		if(cmd == null) return false;
		StringBuffer cmdName = new StringBuffer(label);
		args = cmd.trim(args, cmdName);
		switch(cmd) {
		case ID:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.id")) {
				if (sender instanceof Player) {
					Player player = (Player)sender;
					ItemStack playerItem = player.getItemInHand();

					if (args.length > 0 && args[0].toLowerCase().matches("(mob|creature)")) {
						Entity mob = getTarget(player);
						if (mob instanceof LivingEntity)
							// TODO: parse via CreatureDrop (need to create CreatureDrop.parse(entity)
							sender.sendMessage("OdId: mob details: "+mob.getType().toString()+"@"+CreatureData.parse((LivingEntity)mob).toString());
						else
							sender.sendMessage("No living entity found.");
					} else {
						sender.sendMessage("Otherdrops ID: item in hand is "+playerItem.toString()+" id: "+playerItem.getTypeId()+"@"+playerItem.getDurability()+" maxdura:"+playerItem.getType().getMaxDurability() + " dura%:"+getDurabilityPercentage(playerItem));
						Block block = player.getTargetBlock(null, 100);
						sender.sendMessage("Otherdrops ID: item player is looking at is "+block.toString()+" mat: "+block.getType().toString());
					}
				}
			} else sender.sendMessage("You don't have permission for this command.");
			break;
		case RELOAD:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.reloadconfig")) {
				otherdrops.config.load();
				sender.sendMessage("OtherDrops config reloaded.");
				Log.logInfo("Config reloaded by " + getName(sender) + ".");
			} else sender.sendMessage("You don't have permission to reload the config.");
			break;
		case SHOW:
			if(args.length == 0) {
				sender.sendMessage("Error, no block. Please use /" + cmdName + " <block>");
				return true;
			}
			if(Dependencies.hasPermission(sender, "otherdrops.admin.show")) {
				Target target = OtherDropsConfig.parseTarget(args[0]);
				for(Action action : Action.values())
					showBlockInfo(sender, action, target);
			} else sender.sendMessage("You don't have permission to show the drops for a block.");
			break;
		case SETTINGS:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.profiling")) {
				sender.sendMessage("OtherDrops settings:");
				sender.sendMessage((otherdrops.enabled ? ChatColor.GREEN+"OtherDrops enabled." : ChatColor.RED+"OtherDrops disabled."));
				sender.sendMessage("Verbosity: "+ChatColor.GRAY+OtherDropsConfig.getVerbosity());				
				sender.sendMessage("Disable XP if no default drop: "+ChatColor.GRAY+OtherDropsConfig.disableXpOnNonDefault);				
				sender.sendMessage("Money Precision (for messages): "+ChatColor.GRAY+OtherDropsConfig.moneyPrecision);				
				sender.sendMessage("Use unsafe enchantments: "+ChatColor.GRAY+OtherDropsConfig.enchantmentsUseUnsafe);				
				sender.sendMessage("Ignore enchantment start/maxlevel: "+ChatColor.GRAY+OtherDropsConfig.enchantmentsIgnoreLevel);				
			} else sender.sendMessage("You don't have permission for this command.");
			
			break;
		case ENABLE:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.enabledisable")) {
				if (!otherdrops.enabled) {
					otherdrops.enableOtherDrops();
					sender.sendMessage(ChatColor.GREEN+"OtherDrops enabled.");
				} else {
					sender.sendMessage(ChatColor.GRAY+"OtherDrops is already enabled.");
				}
			} else sender.sendMessage("You don't have permission for this command.");
			break;
		case DISABLE:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.enabledisable")) {
				if (otherdrops.enabled) {
					otherdrops.disableOtherDrops();
					sender.sendMessage(ChatColor.RED+"OtherDrops disabled.");
				} else {
					sender.sendMessage(ChatColor.GRAY+"OtherDrops is already disabled.");
				}
			} else sender.sendMessage("You don't have permission for this command.");
			break;
		case DROP:
			if(Dependencies.hasPermission(sender, "otherdrops.admin.drop")) {
			  if (args.length > 0) {
				Location loc = null;
				Player player = null;
				String playerName = "unknown";
				if (sender instanceof Player) {
					player = (Player)sender;
					playerName = player.getDisplayName();
					//loc = player.getLocation();
					loc = player.getTargetBlock(null, 100).getLocation().add(0,1,0); // (???,  max distance)
				}
				
				if (loc != null) {
					String dropString = "";
					for (String arg : args)
						dropString += arg + " ";
					
					dropString = dropString.substring(0, dropString.length()-1);
					DropType drop = DropType.parse(dropString, "");
					if (drop == null) {
						sender.sendMessage("ODDrop - failed to parse drop.");
						return true;
					}
					
					// TODO: allow a way to get a readable name from the DropType
					// Note: we have to parse the lorename here as it's usually parsed in the actual event trigger (so we know playername)
					if (drop.getLoreName() != null)
						drop.setLoreName(MessageAction.parseVariables(drop.getLoreName(), playerName, "", "", "", "1"));
					
					DropFlags flags = DropType.flags(player, true, false, OtherDrops.rng);
					DropResult dropResult = drop.drop(loc, (Target)null, (Location)null, 1, flags);
					
					String dropped = "[NOTHING]";
					if (dropResult.droppedEntities != null) dropped = dropResult.getDroppedString();
					sender.sendMessage("Dropped: "+dropResult.getQuantity()+"x"+dropped);
				}
			  } else {
			  	sender.sendMessage("Usage: /odd <item/mob>@<data> - drops the given item or mob");
			  }
			} else sender.sendMessage("You don't have permission for this command.");
			break;
		case HEROESTEST:
			if (sender instanceof Player) {
				Player playerSender = (Player)sender;
				if (Dependencies.hasHeroes()) {
					sender.sendMessage("Player is in class: "+playerSender.getDisplayName()+"->"+Dependencies.getHeroes().getCharacterManager().getHero(playerSender).getClass());
					sender.sendMessage("Other players Heroes classes output to server.log");
					for (Player player : Bukkit.getServer().getOnlinePlayers()) {

						Hero heroPlayer = Dependencies.getHeroes().getCharacterManager().getHero(player);
						Log.logInfo("Player:"+player.getDisplayName()+"->"+heroPlayer.getHeroClass().toString() + "("+heroPlayer.getLevel()+")");
					}
				}
			}
		default:
			break;

		}
		return true;
	}
	
	/* "/od show" command - shows conditions and actions for the specified block
	 * 
	 * @param sender The sender requesting the info
	 * @param action The action to show info for
	 * @param block The requested target
	 */
	public void showBlockInfo(CommandSender sender, Action action, Target block) {
		StringBuilder message = new StringBuilder();
		message.append("Block " + block + " (" + action + "):");
		
		DropsList dropGroups = otherdrops.config.blocksHash.getList(action, block);
		int i = 1;
		
		if(dropGroups != null) {
			for(CustomDrop drop : dropGroups) {
				if (drop != null) {
					message.append(" (" + i++ + ")");
					if(drop instanceof GroupDropEvent) addDropInfo(message, (GroupDropEvent) drop);
					else addDropInfo(message, (SimpleDrop) drop);
				}
			}
			sender.sendMessage(message.toString());
		} else return; //sender.sendMessage(message+"No info found.");
	}

	private void addDropConditions(StringBuilder message, CustomDrop drop) {
		Map<String, String> messageMap = new HashMap<String, String>();
		
		// Conditions
		messageMap.put("Agent", drop.getToolString()); // make null if "any"
		messageMap.put("Worlds", drop.getWorldsString());
		messageMap.put("Regions", drop.getRegionsString());
		messageMap.put("Weather", drop.getWeatherString());
		messageMap.put("Block faces", drop.getBlockFacesString());
		messageMap.put("Biomes", drop.getBiomeString());
		messageMap.put("Times", drop.getTimeString());
		messageMap.put("Groups", drop.getGroupsString());
		messageMap.put("Permissions", drop.getPermissionsString());
		messageMap.put("Height", (drop.getHeight() == null)? null : drop.getHeight().toString());
		messageMap.put("Attack range", (drop.getAttackRange() == null)? null : drop.getAttackRange().toString());
		messageMap.put("Light level ", (drop.getLightLevel() == null)? null : drop.getLightLevel().toString());
		// Chance and delay
		messageMap.put("Chance", String.valueOf(drop.getChance())); // make null if = 100
		messageMap.put("Exclusive key", drop.getExclusiveKey());
		messageMap.put("Delay", drop.getDelayRange()); // make null if 0
		
		for (Entry<String, String> entry : messageMap.entrySet()) {
			if (entry.getValue() != null) {
				message.append("\n  §7"+entry.getKey()+":§r "+entry.getValue());
			}
		}
	}

	private void addDropInfo(StringBuilder message, SimpleDrop drop) {
		addDropConditions(message, drop);
		Map<String, String> messageMap = new HashMap<String, String>();

		messageMap.put("Drop", stringHelper(drop.getDropped()));  // TODO: this returns the object, not a string?
		messageMap.put("Quantity", stringHelper(drop.getQuantityRange())); // make null if 1 (Default)
		messageMap.put("Attacker damage", stringHelper(drop.getAttackerDamageRange()));
		messageMap.put("Tool damage", stringHelper(drop.getToolDamage()));
		messageMap.put("Drop spread", drop.getDropSpreadChance() + "% chance");
		messageMap.put("Replacement block", stringHelper(drop.getReplacement()));
		messageMap.put("Commands", stringHelper(drop.getCommands())); // make null if empty list
		messageMap.put("Messages", drop.getMessagesString());
		messageMap.put("Sound effects", drop.getEffectsString());
		messageMap.put("Events", stringHelper(drop.getEvents()));
		
		for (Entry<String, String> entry : messageMap.entrySet()) {
			if (entry.getValue() != null) {
				message.append("\n  §7"+entry.getKey()+":§r "+entry.getValue());
			}
		}

	}
	
	private String stringHelper(Object object) {
		if (object == null) return null;
		else return object.toString();
	}

	private void addDropInfo(StringBuilder message, GroupDropEvent group) {
		addDropConditions(message, group);
		message.append(" Drop group: " + group.getName());
		char j = 'A';
		for(CustomDrop subDrop : group.getDrops()) {
			message.append(" (" + j++ + ")");
			if(j > 'Z') j = 'a';
			if(subDrop instanceof GroupDropEvent) addDropInfo(message, (GroupDropEvent) subDrop);
			else addDropInfo(message, (SimpleDrop) subDrop);
		}
	}
}
