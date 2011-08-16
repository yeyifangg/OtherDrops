package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.entity.Player;

public class PlayerTarget extends Target {
	private Player who;
	
	public PlayerTarget(Player player) {
		super(TargetType.PLAYER);
		who = player;
	}
}
