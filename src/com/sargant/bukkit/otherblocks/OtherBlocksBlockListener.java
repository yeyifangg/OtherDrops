// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.*; 
import org.bukkit.block.Block;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		OtherBlocksDrops.checkDrops(event, parent);				
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		OtherBlocksDrops.checkDrops(event, parent);
	}
	
	@Override
	public void onBlockFromTo(BlockFromToEvent event) {
/*//temp disabled - not working anyway
		if (event.isCancelled()) return;
		if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER)
			return;
		if(event.getToBlock().getType() == Material.AIR) return;

		Block target  = event.getToBlock();
		Integer maxDamage = 0;
		boolean successfulComparison = false;
		boolean doDefaultDrop = false;

		for(OB_Drop obc : parent.transformList) {
		    
		    if(!obc.compareTo(
		            event.getBlock().getType().toString(),
		            (short) event.getBlock().getData(),
		            "DAMAGE_WATER", 
		            target.getWorld(),
		            null,
		            parent.permissionHandler)) {
		        
		        continue;
		    }

		    // Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			// At this point, the tool and the target block match
			successfulComparison = true;
			if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
			OtherBlocks.performDrop(target.getLocation(), obc, null);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulComparison && !doDefaultDrop) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}*/
	}
}

