package com.gmail.zariust.bukkit.otherblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class DropRunner implements Runnable{
		private OtherBlocks plugin;
		Location target;
		OB_Drop dropData;
		Player player;
		Location playerLoc;

		public DropRunner(OtherBlocks otherblocks, Location target, OB_Drop dropData, Player player, Location playerLoc) {
			this.plugin = otherblocks;
			this.target = target;
			this.dropData = dropData;
			this.player = player;
			this.playerLoc = playerLoc;
		}

		@Override
		public void run() {
			plugin.performActualDrop(target, dropData, player, playerLoc);
		}
	
}
