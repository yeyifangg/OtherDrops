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
	
	@Override
	public String getName() {
		return caller.getName();
	}
	
	@Override
	public void sendMessage(String message) {
		if(suppress) super.sendMessage(message);
		else caller.sendMessage(message);
	}
}
