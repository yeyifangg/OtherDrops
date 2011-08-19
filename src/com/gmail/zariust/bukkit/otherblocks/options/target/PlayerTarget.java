package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public class PlayerTarget implements Target {
	private Player who;
	private String name;
	
	public PlayerTarget(Player player) {
		this(player.getName());
		who = player;
	}
	
	public PlayerTarget(String player) {
		name = player;
	}
	
	public Player getPlayer() {
		if(who == null) who = Bukkit.getServer().getPlayer(name);
		return who;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof PlayerTarget)) return false;
		PlayerTarget targ = (PlayerTarget) other;
		return name.equals(targ.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	@Override
	public boolean matches(Target block) {
		return equals(block);
	}
}
