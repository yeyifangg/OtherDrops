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

import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class OdVehicleListener extends VehicleListener {
	private OtherDrops parent;

	public OdVehicleListener(OtherDrops instance)
	{
		parent = instance;
	}

	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		ProfilerEntry entry = new ProfilerEntry("VEHICLEBREAK");
		OtherDrops.profiler.startProfiling(entry);
		OccurredEvent drop = new OccurredEvent(event);
		OtherDrops.logInfo("Vechicle drop occurance created. ("+drop.toString()+")",HIGHEST);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}
}
