package com.gmail.zariust.bukkit.otherblocks;

import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;

public class OtherBlocksVehicleListener extends VehicleListener {
	private OtherBlocks parent;

	public OtherBlocksVehicleListener(OtherBlocks instance)
	{
		parent = instance;
	}

	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		OtherBlocksDrops.checkDrops(event, parent);
	}
}
