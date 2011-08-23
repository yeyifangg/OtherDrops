package com.gmail.zariust.bukkit.otherblocks.listener;

import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.ProfilerEntry;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;

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
		parent.performDrop(drop);
		OtherBlocks.profiler.stopProfiling(entry);
	}
}
