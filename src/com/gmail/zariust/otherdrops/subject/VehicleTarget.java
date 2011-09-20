package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.ContainerData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SimpleData;
import com.gmail.zariust.otherdrops.data.VehicleData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;

public class VehicleTarget implements Target {
	private Material material;
	private Data data;
	private Entity vessel;

	public VehicleTarget(Painting painting) {
		// TODO: Also fetch what painting it is (no API for this yet)
		this(Material.PAINTING, 0);
		vessel = painting;
	}

	public VehicleTarget(Vehicle vehicle) {
		this(CommonEntity.getVehicleType(vehicle), getVehicleData(vehicle));
		vessel = vehicle;
	}

	public VehicleTarget(Material type, int i) {
		this(type, new SimpleData(i));
	}
	
	protected VehicleTarget(Material type, Data d) {
		material = type;
		data = d;
	}

	private static Data getVehicleData(Vehicle vehicle) {
		if(vehicle instanceof StorageMinecart) return new ContainerData((StorageMinecart)vehicle);
		else if(vehicle instanceof PoweredMinecart) return new SimpleData();
		else if(vehicle instanceof Boat || vehicle instanceof Minecart) return new VehicleData(vehicle);
		return null;
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.BLOCK; // TODO: Should we add an ItemCategory.VEHICLE?
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof VehicleTarget)) return false;
		VehicleTarget targ = (VehicleTarget) other;
		return material == targ.material && data.equals(targ.data);
	}
	
	@Override
	public int hashCode() {
		return ((data == null ? 0 : data.getData()) << 16) | material.getId();
	}
	
	@Override
	public boolean matches(Subject block) {
		if(!(block instanceof VehicleTarget)) return false;
		VehicleTarget targ = (VehicleTarget) block;
		
		Boolean match = false;
		if (material == targ.material) match = true;
		if (data == null) {
			match = true;
		} else {
			match = data.matches(targ.data);
		}
		return match;
	}
	
	@Override
	public Location getLocation() {
		if(vessel == null) return null;
		return vessel.getLocation();
	}
	
	@Override
	public boolean overrideOn100Percent() {
		return true;
	}
	
	@Override
	public List<Target> canMatch() {
		return Collections.singletonList((Target)this);
	}
	
	@Override
	public String getKey() {
		return material.toString();
	}
	
	@Override
	public void setTo(BlockTarget replacement) {
		if(vessel == null) {
			OtherDrops.logWarning("VehicleTarget had a null entity; could not remove it and replace with blocks.");
			return;
		}
		// TODO: A way to replace the blocks in all the locations they occupy?
		Block bl = vessel.getLocation().getBlock();
		new BlockTarget(bl).setTo(replacement);
		vessel.remove();
	}
	
	public Entity getVehicle() {
		return vessel;
	}

	@SuppressWarnings("incomplete-switch")
	public static Target parse(Material type, String state) {
		Data data = null;
		try {
			switch(type) {
			case BOAT:
			case MINECART:
				data = VehicleData.parse(type, state);
				break;
			case STORAGE_MINECART:
				data = ContainerData.parse(type, state);
				break;
			case POWERED_MINECART:
			case PAINTING:
				data = SimpleData.parse(type, state);
				break;
			}
		} catch(IllegalArgumentException e) {
			OtherDrops.logWarning(e.getMessage());
			return null;
		}
		return new VehicleTarget(type, data);
	}
}
