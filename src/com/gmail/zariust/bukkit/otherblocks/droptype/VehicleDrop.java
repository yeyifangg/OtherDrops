package com.gmail.zariust.bukkit.otherblocks.droptype;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

public class VehicleDrop extends DropType {
	private Material vessel;
	private int quantity;
	
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
		super(DropCategory.VEHICLE, percent);
		vessel = vehicle;
		quantity = amount;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		World world = where.getWorld();
		int amount = quantity;
		while(amount-- > 0) {
			switch(vessel) {
			case BOAT:
				world.spawn(where, Boat.class);
				break;
			case POWERED_MINECART:
				world.spawn(where, PoweredMinecart.class);
				break;
			case STORAGE_MINECART:
				world.spawn(where, StorageMinecart.class);
				break;
			case MINECART:
				world.spawn(where, Minecart.class);
				break;
			case PAINTING: // Probably won't actually work
				world.spawn(where, Painting.class);
				break;
			default:
			}
		}
	}

	public static DropType parse(String drop, String data, int amount, double chance) {
		String[] split = drop.split("@");
		if(split.length > 1) data = split[1];
		String name = split[0];
		if(name.equals("BOAT")) return new VehicleDrop(amount, Material.BOAT, chance);
		if(name.equals("POWERED_MINECART")) return new VehicleDrop(amount, Material.POWERED_MINECART, chance); // TODO: Power?
		if(name.equals("STORAGE_MINECART")) return new VehicleDrop(amount, Material.STORAGE_MINECART, chance); // TODO: Contents?
		if(name.equals("MINECART")) return new VehicleDrop(amount, Material.MINECART, chance); // TODO: Contents?
		if(name.equals("PAINTING")) return new VehicleDrop(amount, Material.PAINTING, chance); // TODO: Art?
		return null;
	}
}
