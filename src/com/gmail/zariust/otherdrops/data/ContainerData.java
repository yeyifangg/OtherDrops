package com.gmail.zariust.otherdrops.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.zariust.otherdrops.OtherDrops;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.FurnaceAndDispenser;
import org.bukkit.material.MaterialData;

public class ContainerData implements Data {
	// TODO: Should we consider data here?
	private Set<Material> inven;
	private boolean burning, cooking;
	private int facing;
	
	public ContainerData(BlockState state) {
		inven = new HashSet<Material>();
		if(state instanceof ContainerBlock) {
			Inventory inventory = ((ContainerBlock)state).getInventory();
			ItemStack[] contents = inventory.getContents();
			for(ItemStack stack : contents) inven.add(stack.getType());
		}
		if(state instanceof Furnace) {
			Furnace oven = (Furnace) state;
			burning = oven.getBurnTime() > 0;
			cooking = oven.getCookTime() > 0;
		}
		facing = state.getData().getData();
	}

	public ContainerData(StorageMinecart vehicle) {
		Inventory inventory = vehicle.getInventory();
		ItemStack[] contents = inventory.getContents();
		for(ItemStack stack : contents) inven.add(stack.getType());
	}
	
	public ContainerData(Material... materials) {
		this(listToSet(Arrays.asList(materials)));
	}
	
	public ContainerData(Set<Material> materials) {
		this(materials, 0, false, false);
	}

	public ContainerData(int data) {
		this(new HashSet<Material>(), data, false, false);
	}
	
	public ContainerData(boolean burn, boolean cook) {
		this(new HashSet<Material>(), 0, burn, cook);
	}
	
	public ContainerData(int data, boolean burn, boolean cook) {
		this(new HashSet<Material>(), data, burn, cook);
	}
	
	public ContainerData(Set<Material> items, int data, boolean burn, boolean cook) {
		inven = items;
		facing = data;
		burning = burn;
		cooking = cook;
	}

	private ContainerData() {}

	private static Set<Material> listToSet(List<Material> list) {
		Set<Material> set = new HashSet<Material>();
		set.addAll(list);
		return set;
	}

	@Override
	public int getData() {
		return facing;
	}
	
	@Override
	public void setData(int d) {
		facing = d;
	}
	
	@Override
	public boolean matches(Data d) {
		if(!(d instanceof ContainerData)) return false;
		ContainerData container = (ContainerData) d;
		if(burning != container.burning || cooking != container.cooking || facing != container.facing)
			return false;
		return inven.containsAll(container.inven);
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat instanceof Material) return get((Material)mat);
		return "";
	}
	
	@SuppressWarnings("incomplete-switch")
	private String get(Material mat) {
		String result = "";
		switch(mat) {
		case FURNACE:
		case BURNING_FURNACE:
			if(burning) result += "BURNING/";
			if(cooking) result += "COOKING/";
			// Fallthrough intentional
		case DISPENSER:
			FurnaceAndDispenser fd = new FurnaceAndDispenser(mat, (byte)facing);
			result += fd.getFacing().toString();
			// Fallthrough intentional
		case STORAGE_MINECART:
		case CHEST:
			for(Material item : inven) result += "/" + item;
		}
		return result;
	}

	@Override
	public void setOn(BlockState state) {
		if(!(state instanceof ContainerBlock)) {
			OtherDrops.logWarning("Tried to change a container block, but no container was found!");
			return;
		}
		ContainerBlock block = (ContainerBlock) state;
		for(Material item : inven) block.getInventory().addItem(new ItemStack(item, 1));
		state.setData(new MaterialData(state.getType(), (byte)facing));
		// TODO: Should we set burn time and cook time if it's a furnace? To what values?
	}

	@Override
	public void setOn(Entity entity, Player witness) {
		if(!(entity instanceof StorageMinecart)) {
			OtherDrops.logWarning("Tried to change a storage cart, but no container was found!");
			return;
		}
		StorageMinecart cart = (StorageMinecart) entity;
		for(Material item : inven) cart.getInventory().addItem(new ItemStack(item, 1));
	}

	@SuppressWarnings("incomplete-switch")
	public static Data parse(Material mat, String state) {
		ContainerData ret = new ContainerData();
		List<String> args = Arrays.asList(state.split("/"));
		switch(mat) {
		case FURNACE:
		case BURNING_FURNACE:
			ret.burning = args.contains("BURNING");
			ret.cooking = args.contains("COOKING");
			// Fallthrough intentional
		case DISPENSER:
			FurnaceAndDispenser fd = new FurnaceAndDispenser(mat);
			fd.setFacingDirection(BlockFace.valueOf(state));
			ret.facing = fd.getData();
			// Fallthrough intentional
		case STORAGE_MINECART:
		case CHEST:
			for(String arg : args) ret.inven.add(Material.getMaterial(arg));
		}
		return ret;
	}
}
