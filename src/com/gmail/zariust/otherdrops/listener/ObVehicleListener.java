package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;

import com.gmail.zariust.otherdrops.OtherBlocks;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.event.OccurredDrop;

public class ObVehicleListener extends VehicleListener {
	private OtherBlocks parent;

	public ObVehicleListener(OtherBlocks instance)
	{
		parent = instance;
	}

	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		ProfilerEntry entry = new ProfilerEntry("VEHICLEBREAK");
		OtherBlocks.profiler.startProfiling(entry);
		OccurredDrop drop = new OccurredDrop(event);
		OtherBlocks.logInfo("Vechicle drop occurance created. ("+drop.toString()+")",4);
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}
}
