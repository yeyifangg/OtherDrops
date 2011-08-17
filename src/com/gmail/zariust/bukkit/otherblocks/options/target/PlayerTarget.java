package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerTarget extends Target {
	private Player who;
	private String name;
	
	public PlayerTarget(Player player) {
		this(player.getName());
		who = player;
	}
	
	public PlayerTarget(String player) {
		super(TargetType.PLAYER);
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
}
