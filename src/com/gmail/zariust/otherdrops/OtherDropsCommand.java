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

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.GroupDropEvent;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class OtherDropsCommand implements CommandExecutor {
	private enum OBCommand {
		ID("id", "i"),
		RELOAD("reload", "r"),
		SHOW("show", "s"),
		PROFILE("profile", "p");
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
				if(arg && firstArg.equalsIgnoreCase(cmd.cmdName)) return cmd;
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
	private OtherDrops otherdrops;
	
	public OtherDropsCommand(OtherDrops plugin) {
		otherdrops = plugin;
	}
	
	private String getName(CommandSender sender) {
		if(sender instanceof ConsoleCommandSender) return "CONSOLE";
		else if(sender instanceof Player) return ((Player) sender).getName();
		else return "UNKNOWN";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		OBCommand cmd = OBCommand.match(label, args.length >= 1 ? args[0] : "");
		if(cmd == null) return false;
		StringBuffer cmdName = new StringBuffer(label);
		args = cmd.trim(args, cmdName);
		switch(cmd) {
		case ID:
			if(otherdrops.hasPermission(sender, "otherdrops.admin.id")) {
				if (sender instanceof Player) {
					Player player = (Player)sender;
					ItemStack playerItem = player.getItemInHand();
					sender.sendMessage("Otherdrops ID: item in hand is "+playerItem.toString()+" id: "+playerItem.getTypeId()+"@"+playerItem.getDurability());			
				}
			}
			break;
		case RELOAD:
			if(otherdrops.hasPermission(sender, "otherdrops.admin.reloadconfig")) {
				otherdrops.config.load();
				sender.sendMessage("OtherDrops config reloaded.");
				OtherDrops.logInfo("Config reloaded by " + getName(sender) + ".");
			} else sender.sendMessage("You don't have permission to reload the config.");
			break;
		case SHOW:
			if(args.length == 0) {
				sender.sendMessage("Error, no block. Please use /" + cmdName + " <block>");
				return true;
			}
			if(otherdrops.hasPermission(sender, "otherdrops.admin.show")) {
				Target target = OtherDropsConfig.parseTarget(args[0]);
				for(Action action : Action.values())
					showBlockInfo(sender, action, target);
			} else sender.sendMessage("You don't have permission to show the drops for a block.");
			break;
		case PROFILE:
			if(otherdrops.hasPermission(sender, "otherdrops.admin.profiling"))
				profilingCommand(sender, args);
			else sender.sendMessage("You don't have permission to manage profiling for OtherDrops.");
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
				message.append(" (" + i++ + ")");
				if(drop instanceof GroupDropEvent) addDropInfo(message, (GroupDropEvent) drop);
				else addDropInfo(message, (SimpleDrop) drop);
			}
			sender.sendMessage(message.toString());
		} else sender.sendMessage(message+"No info found.");
	}

	private void addDropConditions(StringBuilder message, CustomDrop drop) {
		// Conditions
		message.append(" Agent: " + drop.getToolString());
		message.append(" Worlds: " + drop.getWorldsString());
		message.append(" Regions: " + drop.getRegionsString());
		message.append(" Weather: " + drop.getWeatherString());
		message.append(" Block faces: " + drop.getBlockFacesString());
		message.append(" Biomes: " + drop.getBiomeString());
		message.append(" Times: " + drop.getTimeString());
		message.append(" Groups: " + drop.getGroupsString());
		message.append(" Permissions: " + drop.getPermissionsString());
		message.append(" Height: " + drop.getHeight());
		message.append(" Attack range: " + drop.getAttackRange());
		message.append(" Light level: " + drop.getLightLevel());
		// Chance and delay
		message.append(" Chance: " + drop.getChance());
		message.append(" Exclusive key: " + drop.getExclusiveKey());
		message.append(" Delay: " + drop.getDelayRange());
	}

	private void addDropInfo(StringBuilder message, SimpleDrop drop) {
		addDropConditions(message, drop);
		message.append(" Drop: " + drop.getDropped());  // TODO: this returns the object, not a string?
		message.append(" Quantity: " + drop.getQuantityRange());
		message.append(" Attacker damage: " + drop.getAttackerDamageRange());
		message.append(" Tool damage: " + drop.getToolDamage());
		message.append(" Drop spread: " + drop.getDropSpreadChance() + "% chance");
		message.append(" Replacement block: " + drop.getReplacement());
		message.append(" Commands: " + drop.getCommands());
		message.append(" Messages: " + drop.getMessagesString());
		message.append(" Sound effects: " + drop.getEffectsString());
		message.append(" Events: " + drop.getEvents());
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

	/* "/od profile" command - turns profiling on/off or shows profile information for particular event.
	 * 
	 * @param sender CommandSender from Bukkit onCommand() function - can be a player or console
	 * @param args   String list of command arguments from Bukkit onCommand() function
	 */
	public void profilingCommand(CommandSender sender, String[] args) {
	    if(args.length < 1) {
	    	sender.sendMessage("Usage: /od profile <cmd> (cmd = on/off/list/nano/<event> [avg])");
	        return;
	    }
	    
	    if(args[0].equalsIgnoreCase("off")) {
	        otherdrops.config.profiling = false;
	        OtherDrops.profiler.clearProfiling();
	        sender.sendMessage("Profiling stopped, profiling data cleared.");
	    } else if(args[0].equalsIgnoreCase("on")) {
	    	otherdrops.config.profiling = true;
	        sender.sendMessage("Profiling started...");
	    } else if(args[0].equalsIgnoreCase("nano")) {
	    	OtherDrops.profiler.setNano(!OtherDrops.profiler.getNano());
	    	sender.sendMessage("Profiler: time set to "+(OtherDrops.profiler.getNano()?" nanoseconds.":" milliseconds."));
	    } else if(args[0].equalsIgnoreCase("list")) {
	    	sender.sendMessage("Possible events: leafdecay/blockbreak/blockflow/entitydeath/interact/");
	    	sender.sendMessage("paintingbreak/vehiclebreak/explode");
	    } else {
	        if(otherdrops.config.profiling) {
    	        List<Long> profileData = OtherDrops.profiler.getProfiling(args[0].toUpperCase());
    	        if(profileData == null || profileData.isEmpty()) {
    	        	sender.sendMessage("No data found.");   
    	        } else {
    	            boolean showAverage = false;
    	            if(args.length >= 2) {
    	                if (args[1].equalsIgnoreCase("avg")) showAverage = true;
    	            }
    	            if(showAverage) {
    	                long average = 0L;
    	                long total = 0L;
    	                for (long profileBit : profileData) {
    	                    total = total + profileBit;
    	                }
    	                average = total / profileData.size();
    	                sender.sendMessage("average: " + average);
    	            } else {
    	            	sender.sendMessage(profileData.toString());
    	            }
    	        }
	        } else sender.sendMessage("Profiling is currently off - please turn on with /od profile on");
	    }
	}
}
