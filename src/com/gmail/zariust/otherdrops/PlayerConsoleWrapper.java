package com.gmail.zariust.otherdrops;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlayerConsoleWrapper extends ConsoleCommandSender {
	boolean suppress;
	Player caller;
	
	public PlayerConsoleWrapper(Player player, boolean suppressMessages) {
		super(Bukkit.getServer());
		caller = player;
		suppress = suppressMessages;
	}
	
	public String getName() {
		return caller.getName();
	}
	
	@Override
	public void sendMessage(String message) {
		if(suppress) super.sendMessage(message);
		else caller.sendMessage(message);
	}
}
