package com.gmail.zariust.bukkit.otherblocks;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.otherblocks.drops.CustomDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.DropGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.DropsList;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.subject.Target;

public class OtherBlocksCommand implements CommandExecutor {
	private enum OBCommand {
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
			if(label.equalsIgnoreCase("ob")) arg = true;
			for(OBCommand cmd : values()) {
				if(arg && firstArg.equalsIgnoreCase(cmd.cmdName)) return cmd;
				else if(label.equalsIgnoreCase("ob" + cmd.cmdShort) || label.equalsIgnoreCase("ob" + cmd.cmdName))
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
	private OtherBlocks otherblocks;
	
	public OtherBlocksCommand(OtherBlocks plugin) {
		otherblocks = plugin;
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
		case RELOAD:
			if(otherblocks.hasPermission(sender, "otherblocks.admin.reloadconfig")) {
				otherblocks.config.load();
				sender.sendMessage("OtherBlocks config reloaded.");
				OtherBlocks.logInfo("Config reloaded by " + getName(sender) + ".");
			} else sender.sendMessage("You don't have permission to reload the config.");
			break;
		case SHOW:
			if(args.length == 0) {
				sender.sendMessage("Error, no block. Please use /" + cmdName + " <block>");
				return true;
			}
			if(otherblocks.hasPermission(sender, "otherblocks.admin.show")) {
				Target target = OtherBlocksConfig.parseTarget(args[0]);
				for(Action action : Action.values())
					showBlockInfo(sender, action, target);
			} else sender.sendMessage("You don't have permission to show the drops for a block.");
			break;
		case PROFILE:
			if(otherblocks.hasPermission(sender, "otherblocks.admin.profiling"))
				profilingCommand(sender, args);
			else sender.sendMessage("You don't have permission to manage profiling for OtherBlocks.");
			break;
		}
		return true;
	}
	
	/* "/ob show" command - shows conditions and actions for the specified block
	 * 
	 * @param sender The sender requesting the info
	 * @param action The action to show info for
	 * @param block The requested target
	 */
	public void showBlockInfo(CommandSender sender, Action action, Target block) {
		StringBuilder message = new StringBuilder();
		message.append("Block " + block + " (" + action + "):");
		
		DropsList dropGroups = otherblocks.config.blocksHash.getList(action, block);
		int i = 1;
		
		if(dropGroups != null) {
			for(CustomDrop drop : dropGroups.list) {
				message.append(" (" + i++ + ")");
				if(drop instanceof DropGroup) {
					addDropConditions(message, drop);
					DropGroup group = (DropGroup) drop;
					message.append(" Drop group: " + group.getName());
					char j = 'A';
					for(SimpleDrop subDrop : group.getDrops()) {
						message.append(" (" + j++ + ")");
						if(j > 'Z') j = 'a';
						addDropInfo(message, subDrop);
					}
				} else addDropInfo(message, (SimpleDrop) drop);
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
		message.append(" Drop: " + drop.getDropped());
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

	/* "/ob profile" command - turns profiling on/off or shows profile information for particular event.
	 * 
	 * @param sender CommandSender from Bukkit onCommand() function - can be a player or console
	 * @param args   String list of command arguments from Bukkit onCommand() function
	 */
	public void profilingCommand(CommandSender sender, String[] args) {
	    if(args.length < 1) {
	    	sender.sendMessage("Usage: /ob profile <cmd> (cmd = on/off/list/<event> [avg])");
	        return;
	    }
	    
	    if(args[0].equalsIgnoreCase("off")) {
	        otherblocks.config.profiling = false;
	        otherblocks.clearProfiling();
	        sender.sendMessage("Profiling stopped, profiling data cleared.");
	    } else if(args[0].equalsIgnoreCase("on")) {
	    	otherblocks.config.profiling = true;
	        sender.sendMessage("Profiling started...");
	    } else if(args[0].equalsIgnoreCase("list")) {
	    	sender.sendMessage("Possible events: leafdecay/blockbreak/blockflow/entitydeath/interact/");
	    	sender.sendMessage("paintingbreak/vehiclebreak/explode");
	    } else {
	        if(otherblocks.config.profiling) {
    	        List<Long> profileData = otherblocks.getProfiling(args[1].toUpperCase());
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
	        } else sender.sendMessage("Profiling is currently off - please turn on with /ob profile on");
	    }
	}
}
