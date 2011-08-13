package com.gmail.zariust.bukkit.otherblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.otherblocks.drops.CustomDrop;


public class DropRunner implements Runnable{
		private OtherBlocks plugin;
		Object target;
		CustomDrop dropData;
		Player player;
		Location playerLoc;

		public DropRunner(OtherBlocks otherblocks, Object target, CustomDrop dropData, Player player, Location playerLoc) {
			this.plugin = otherblocks;
			this.target = target;
			this.dropData = dropData;
			this.player = player;
			this.playerLoc = playerLoc;
		}

		@Override
		public void run() {
			plugin.performDrop_Passer(target, dropData, player, playerLoc);
		}
	
}
