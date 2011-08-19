package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemDrop extends DropType {
	private ItemStack item;
	
	public ItemDrop(Material mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(MaterialData mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(Material mat, int amount) {
		this(mat, amount, 100.0);
	}

	public ItemDrop(MaterialData mat, int amount) {
		this(mat, amount, 100.0);
	}

	public ItemDrop(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public ItemDrop(Material mat, double percent) {
		this(mat.getNewData((byte) 0), percent);
	}
	
	public ItemDrop(MaterialData mat, double percent) {
		this(mat.toItemStack(), percent);
	}
	
	public ItemDrop(Material mat, int amount, double percent) {
		this(mat.getNewData((byte) 0), amount, percent);
	}
	
	public ItemDrop(MaterialData mat, int amount, double percent) {
		this(mat.toItemStack(amount), percent);
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		super(DropCategory.ITEM, percent);
		item = stack;
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		if(flags.spread) {
			ItemStack stack = new ItemStack(item.getType(), 1, item.getDurability());
			int count = item.getAmount();
			while(count-- > 0) drop(where, stack, flags.naturally);
		} else drop(where, item, flags.naturally);
	}
}
