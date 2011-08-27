// OtherDrops - a Bukkit plugin
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

package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;

public class OdPlayerListener extends PlayerListener
{
	private OtherDrops parent;

	public OdPlayerListener(OtherDrops instance) {
		parent = instance;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		ProfilerEntry entry = new ProfilerEntry("INTERACT");
		OtherDrops.profiler.startProfiling(entry);
		OccurredDropEvent drop = new OccurredDropEvent(event);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}

	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.isCancelled()) return;
		ProfilerEntry entry = new ProfilerEntry("INTERACT");
		OtherDrops.profiler.startProfiling(entry);
		OccurredDropEvent drop = new OccurredDropEvent(event);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}
}

