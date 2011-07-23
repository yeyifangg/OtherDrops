package com.gmail.zariust.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.material.Colorable;

import com.gmail.zariust.bukkit.common.CommonEntity;

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
