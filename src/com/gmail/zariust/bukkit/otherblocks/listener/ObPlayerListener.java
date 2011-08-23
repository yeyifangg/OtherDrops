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

package com.gmail.zariust.bukkit.otherblocks.listener;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.ProfilerEntry;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;

public class ObPlayerListener extends PlayerListener
{
	private OtherBlocks parent;

	public ObPlayerListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		ProfilerEntry entry = new ProfilerEntry("INTERACT");
		OtherBlocks.profiler.startProfiling(entry);
		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}

	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.isCancelled()) return;
		ProfilerEntry entry = new ProfilerEntry("INTERACT");
		OtherBlocks.profiler.startProfiling(entry);
		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}
}

