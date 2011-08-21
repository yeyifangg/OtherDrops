package com.gmail.zariust.bukkit.otherblocks.droptype;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public class ItemDrop extends DropType {
	private ItemStack item;
	
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
		this(new ItemStack(mat, amount, (short) data), percent);
	}
	
	public ItemDrop(ItemStack stack, double percent) { // Rome
		super(DropCategory.ITEM, percent);
		item = stack;
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		if(item.getAmount() == 0) return;
		if(flags.spread) {
			ItemStack stack = new ItemStack(item.getType(), 1, item.getDurability());
			int count = item.getAmount();
			while(count-- > 0) drop(where, stack, flags.naturally);
		} else drop(where, item, flags.naturally);
	}

	public static DropType parse(String drop, String defaultData, int amount, double chance) {
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
		Integer data = null;
		try {
			data = CommonMaterial.parseItemData(mat, defaultData);
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data, chance);
		return new ItemDrop(amount, mat, chance);
	}
}
