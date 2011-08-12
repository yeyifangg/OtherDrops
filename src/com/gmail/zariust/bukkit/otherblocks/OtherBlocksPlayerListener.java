// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
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

package com.gmail.zariust.bukkit.otherblocks;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class OtherBlocksPlayerListener extends PlayerListener
{
	private OtherBlocks parent;

	public OtherBlocksPlayerListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;

	//	if (!OtherBlocksConfig.dropForInteract) return;
		OtherBlocksDrops.checkDrops(event, parent);
	}

	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.isCancelled()) return;		
		OtherBlocksDrops.checkDrops(event, parent);
		
		/*
		Entity entity = event.getRightClicked();
		Player player = event.getPlayer();
		ItemStack tool = player.getItemInHand();
		String toolString = tool.getType().toString();
		OtherBlocks.logInfo(toolString);
		if (toolString.equalsIgnoreCase("SHEARS")) {
			event.setCancelled(true);
		}
		if(entity instanceof Sheep) {
			Sheep sheep = (Sheep) entity;
			if (!sheep.isSheared()) {
				int quantity = 20;
				for (int i = 0; i < quantity; i++) {
					entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.WOOL, 1, (short)1));
				}
				sheep.setSheared(true);
			} else {
				if (toolString == "GLASS") {
					sheep.setSheared(false);
				}
			}
		}*/

	}
}

