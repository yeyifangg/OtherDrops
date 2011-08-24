package com.gmail.zariust.bukkit.otherblocks.droptype;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.data.ItemData;

public class ItemDrop extends DropType {
	Material material;
	ItemData durability;
	int quantity;;
	
	public ItemDrop(Material mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(Material mat, int data) {
		this(mat, data, 100.0);
	}
	
	public ItemDrop(int amount, Material mat) {
		this(amount, mat, 100.0);
	}

	public ItemDrop(int amount, Material mat, int data) {
		this(amount, mat, data, 100.0);
	}

	public ItemDrop(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public ItemDrop(Material mat, double percent) {
		this(mat, 0, percent);
	}
	
	public ItemDrop(Material mat, int data, double percent) {
		this(new ItemStack(mat, 1, (short) data), percent);
	}
	
	public ItemDrop(int amount, Material mat, double percent) {
		this(amount, mat, 0, percent);
	}
	
	public ItemDrop(int amount, Material mat, int data, double percent) {
		this(amount, mat, new ItemData(data), percent);
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		this(stack.getAmount(), stack.getType(), new ItemData(stack), percent);
	}
	
	public ItemDrop(int amount, Material mat, ItemData data, double percent) { // Rome
		super(DropCategory.ITEM, percent);
		quantity = amount;
		material = mat;
		durability = data;
	}

	public ItemStack getItem() {
		return new ItemStack(material, quantity, (short)durability.getData());
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		if(quantity == 0) return;
		if(flags.spread) {
			ItemStack stack = new ItemStack(material, 1, (short)durability.getData());
			int count = quantity;
			while(count-- > 0) drop(where, stack, flags.naturally);
		} else drop(where, getItem(), flags.naturally);
	}

	public static DropType parse(String drop, String defaultData, int amount, double chance) {
		drop = drop.toUpperCase();
		Material mat = Material.getMaterial(drop);
		if(mat == null) {
			if(drop.equalsIgnoreCase("NOTHING")) mat = Material.AIR;
			else if(drop.equalsIgnoreCase("DYE")) mat = Material.INK_SACK;
			else return null;
		}
		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(defaultData);
			return new ItemDrop(amount, mat, d, chance);
		} catch(NumberFormatException e) {}
		ItemData data = null;
		try {
			data = ItemData.parse(mat, defaultData);
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data.getData(), chance);
		return new ItemDrop(amount, mat, chance);
	}

	@Override
	public String toString() {
		String ret = material.toString();
		// TODO: Will durability ever be null, or will it just be 0?
		if(durability != null) ret += "@" + durability.get(material);
		return ret;
	}
}
