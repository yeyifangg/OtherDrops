package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.data.ContainerData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.VehicleData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

public class VehicleDrop extends DropType {
	private Material vessel;
	private int quantity;
	private Data data;
	
	public VehicleDrop(Material vehicle) {
		this(1, vehicle);
	}
	
	public VehicleDrop(Material vehicle, double percent) {
		this(1, vehicle, percent);
	}

	public VehicleDrop(int amount, Material vehicle) {
		this(amount, vehicle, 100.0);
	}

	public VehicleDrop(int amount, Material vehicle, double percent) {
		this(amount, vehicle, null, percent);
	}

	public VehicleDrop(int amount, Material vehicle, Data d, double percent) {
		super(DropCategory.VEHICLE, percent);
		vessel = vehicle;
		quantity = amount;
		data = d;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		World world = where.getWorld();
		int amount = quantity;
		while(amount-- > 0) {
			Entity entity;
			switch(vessel) {
			case BOAT:
				entity = world.spawn(offsetLocation, Boat.class);
				break;
			case POWERED_MINECART:
				entity = world.spawn(offsetLocation, PoweredMinecart.class);
				break;
			case STORAGE_MINECART:
				entity = world.spawn(offsetLocation, StorageMinecart.class);
				break;
			case MINECART:
				entity = world.spawn(offsetLocation, Minecart.class);
				break;
			case PAINTING: // Probably won't actually work
				entity = world.spawn(offsetLocation, Painting.class);
				break;
			default:
				continue;
			}
			data.setOn(entity, flags.recipient);
		}
	}

	public static DropType parse(String drop, String data, int amount, double chance) {
		drop = drop.toUpperCase().replace("VEHICLE_", "");
		String[] split = drop.split("@");
		if(split.length > 1) data = split[1];
		String name = split[0];
		if(name.equals("BOAT"))
			return new VehicleDrop(amount, Material.BOAT, chance);
		if(name.equals("POWERED_MINECART"))
			return new VehicleDrop(amount, Material.POWERED_MINECART, chance); // TODO: Power? (needs API?)
		if(name.equals("STORAGE_MINECART")) {
			Data state = ContainerData.parse(Material.STORAGE_MINECART, data);
			return new VehicleDrop(amount, Material.STORAGE_MINECART, state, chance);
		}
		if(name.equals("MINECART")) {
			Data state = VehicleData.parse(Material.MINECART, data);
			return new VehicleDrop(amount, Material.MINECART, state, chance);
		}
		if(name.equals("PAINTING"))
			return new VehicleDrop(amount, Material.PAINTING, chance); // TODO: Art? (needs API)
		return null;
	}

	@Override
	public String toString() {
		String ret = "VEHICLE_" + vessel.toString();
		if(data != null) ret += "@" + data.get(vessel);
		return ret;
	}

	@Override
	public double getAmount() {
		return quantity;
	}
}
