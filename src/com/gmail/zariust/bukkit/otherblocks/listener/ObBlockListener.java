// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
// Copyright (C) 2011 Robert Sargant
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

package com.gmail.zariust.bukkit.otherblocks.listener;

import org.bukkit.block.Block;
import org.bukkit.event.block.*;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class ObBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public ObBlockListener(OtherBlocks instance) {
		parent = instance;
	}

	public Boolean checkWorldguardLeafDecayPermission(Block block) {
		if (OtherBlocks.worldguardPlugin != null) {
			// WORLDGUARD: check to see if leaf decay is allowed...
			// Need to convert the block (it's location) to a WorldGuard Vector
			Vector pt = BukkitUtil.toVector(block); // TODO: fails if WorldEdit plugin not installed?
			//Location loc = block.getLocation();
			//Vector pt = new Vector(loc.getX(), loc.getY(), loc.getZ());

			// Get the region manager for this world
			RegionManager regionManager = OtherBlocks.worldguardPlugin.getGlobalRegionManager().get(block.getWorld());
			// Get the "set" for this location
			ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
			// If leaf decay is not allowed, just exit this function
			if (!set.allows(DefaultFlag.LEAF_DECAY)) {
				OtherBlocks.logInfo("Leaf decay denied - worldguard protected region.",4);
				return false;
			}
		}
		OtherBlocks.logInfo("Leaf decay allowed.",4);
		return true;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		if (event.isCancelled()) return;
		if (!parent.config.dropForBlocks) return;
		if (!checkWorldguardLeafDecayPermission(event.getBlock())) return;
		parent.startProfiling("LEAFDECAY");

		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);		

		parent.stopProfiling("LEAFDECAY");
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		// TODO: get this dropForBlocks check working again - or perhaps just disable the event listener for blocks (if we can disable it)
		// note: cannot just place a check on the onEnable event listener registration as that wont work with /obr
		//if (!parent.config.dropForBlocks) return;
		parent.startProfiling("BLOCKBREAK");

		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);
		
		parent.stopProfiling("BLOCKBREAK");
	}
	
	@Override
	public void onBlockFromTo(BlockFromToEvent event) {
		if(event.isCancelled()) return;
		if(!parent.config.enableBlockTo) return;
		parent.startProfiling("BLOCKFLOW");
		
		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);
		
		parent.stopProfiling("BLOCKFLOW");
	}
}

