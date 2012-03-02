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

package com.gmail.zariust.otherdrops.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class OdBlockListener implements Listener
{
	private OtherDrops parent;

	public OdBlockListener(OtherDrops instance) {
		parent = instance;
	}

	public Boolean checkWorldguardLeafDecayPermission(Block block) {
		if (OtherDrops.worldguardPlugin != null) {
			// WORLDGUARD: check to see if leaf decay is allowed...
			// Need to convert the block (it's location) to a WorldGuard Vector
			Location loc = block.getLocation();
			Vector pt = new Vector(loc.getX(), loc.getY(), loc.getZ());

			// Get the region manager for this world
			RegionManager regionManager = OtherDrops.worldguardPlugin.getGlobalRegionManager().get(block.getWorld());
			// Get the "set" for this location
			ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
			// If leaf decay is not allowed, just exit this function
			if (!set.allows(DefaultFlag.LEAF_DECAY)) {
				OtherDrops.logInfo("Leaf decay denied - worldguard protected region.",HIGHEST);
				return false;
			}
		}
		OtherDrops.logInfo("Leaf decay allowed.",HIGHEST);
		return true;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onLeavesDecay(LeavesDecayEvent event) {
		if (event.isCancelled()) return;
		if (!parent.config.dropForBlocks) return;
		if (!checkWorldguardLeafDecayPermission(event.getBlock())) return;
		ProfilerEntry entry = new ProfilerEntry("LEAFDECAY");
		OtherDrops.profiler.startProfiling(entry);

		OccurredEvent drop = new OccurredEvent(event);
		parent.performDrop(drop);		

		OtherDrops.profiler.stopProfiling(entry);
	}

	private boolean checkBlockProtected(Block block) {
		if (OtherDrops.mobArenaHandler != null) {
			if(OtherDrops.mobArenaHandler.inEnabledRegion(block.getLocation())) return true;
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		if (!parent.config.dropForBlocks) return;
		if (checkBlockProtected(event.getBlock())) return;
		
		ProfilerEntry entry = new ProfilerEntry("BLOCKBREAK");
		OtherDrops.profiler.startProfiling(entry);

		if (event.getPlayer() != null) if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			// skip drops for creative mode - TODO: make this configurable?
		} else {
			OccurredEvent drop = new OccurredEvent(event);
			parent.performDrop(drop);
		}
		OtherDrops.profiler.stopProfiling(entry);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		if(event.isCancelled()) return;
		if(!parent.config.enableBlockTo) return;
		ProfilerEntry entry = new ProfilerEntry("BLOCKFLOW");
		OtherDrops.profiler.startProfiling(entry);
		
		OccurredEvent drop = new OccurredEvent(event);
		parent.performDrop(drop);
		
		OtherDrops.profiler.stopProfiling(entry);
	}
}

