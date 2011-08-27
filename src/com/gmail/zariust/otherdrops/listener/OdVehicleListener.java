package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;

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
		OccurredDropEvent drop = new OccurredDropEvent(event);
		OtherDrops.logInfo("Vechicle drop occurance created. ("+drop.toString()+")",4);
		parent.performDrop(drop);
		OtherDrops.profiler.stopProfiling(entry);
	}
}
