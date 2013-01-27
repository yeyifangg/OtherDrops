// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class ItemDrop extends DropType {
	private Material material;
	private Data durability;
	private IntRange quantity;
	private int rolledQuantity;
	private Map<Enchantment, Integer> enchantments;
	public ItemDrop(Material mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(Material mat, int data) {
		this(mat, data, 100.0);
	}
	
	public ItemDrop(IntRange amount, Material mat) {
		this(amount, mat, 100.0, null);
	}

	public ItemDrop(IntRange amount, Material mat, int data) {
		this(amount, mat, data, 100.0, null);
	}

	public ItemDrop(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public ItemDrop(Material mat, double percent) {
		this(mat, 0, percent);
	}
	
	public ItemDrop(Material mat, int data, double percent) {
		this(mat == null ? null : new ItemStack(mat, 1, (short) data), percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, double percent, Map<Enchantment, Integer> enchantment, String loreName) {
		this(amount, mat, 0, percent, enchantment);
		
		this.loreName = loreName;
	}

	public ItemDrop(IntRange amount, Material mat, double percent, Map<Enchantment, Integer> enchantment) {
		this(amount, mat, 0, percent, enchantment);
	}
	
	public ItemDrop(IntRange amount, Material mat, int data, double percent, Map<Enchantment, Integer> enchantment) {
		this(amount, mat, new ItemData(data), percent, enchantment, "");
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		this(new IntRange(stack == null ? 1 : stack.getAmount()), stack == null ? null : stack.getType(), stack == null ? null : new ItemData(stack), percent, null, "");
	}
	
	public ItemDrop(IntRange amount, Material mat, Data data, double percent, Map<Enchantment, Integer> enchPass, String loreName) { // Rome
		super(DropCategory.ITEM, percent);
		quantity = amount;
		material = mat;
		durability = data;
		this.enchantments = enchPass;
		this.loreName = loreName;
	}

	public ItemStack getItem(Random rng, int data) {
		rolledQuantity = quantity.getRandomIn(rng);
		ItemStack stack = new ItemStack(material, rolledQuantity, (short)data);
		if (enchantments != null) {
			stack = CommonEnchantments.applyEnchantments(stack, enchantments);
		}
		
		return stack;
	}

	@Override
	protected int performDrop(Target source, Location where, DropFlags flags, OccurredEvent occurrence) {
		occurrence.setOverrideDefault(this.overrideDefault);
		
		int quantityActuallyDropped = 0;
		if(material == null) return 0;
		if(quantity.getMax() == 0) return 0;

		// check if data is THIS (-1) and get accordingly
		int itemData = durability.getData();
		if (itemData == -1) { // ie. itemData = THIS
			String[] dataSplit = source.toString().split("@");
			if (material.toString().equalsIgnoreCase("monster_egg")) { // spawn egg
				EntityType creatureType = CommonEntity.getCreatureEntityType(dataSplit[0]);
				if (creatureType != null) itemData = creatureType.getTypeId();
			} else {
				if (dataSplit.length > 1) itemData = ItemData.parse(material, dataSplit[1].replaceAll("SHEARED/", "")).getData(); // for wool, logs, etc
			}
			if (itemData == -1) itemData = 0; // reset to default data if we weren't able to parse anything else
		}

		if(flags.spread) {				
			ItemStack stack = new ItemStack(material, 1, (short)itemData);
			if (enchantments != null) {
				stack = CommonEnchantments.applyEnchantments(stack, enchantments);
			}
			int count = quantity.getRandomIn(flags.rng);
			rolledQuantity = count;
			while(count-- > 0) quantityActuallyDropped += drop(where, stack, flags.naturally);
		} else {
			quantityActuallyDropped += drop(where, getItem(flags.rng, itemData), flags.naturally);
		}
		
		if (DropType.actuallyDropped != null && !(loreName.isEmpty())) {
			Item is = (Item)DropType.actuallyDropped;
			ItemMeta im = is.getItemStack().getItemMeta();
			
			im.setDisplayName(loreName);
			is.getItemStack().setItemMeta(im);
		}
		return quantityActuallyDropped;

	}

	public static DropType parse(String drop, String defaultData, IntRange amount, double chance) {
		drop = drop.toUpperCase();
		drop = CommonMaterial.substituteAlias(drop);
		String state = defaultData;
		String loreName = "";
		String[] split = drop.split("@");
		drop = split[0];

		Map <Enchantment, Integer> enchPass = new HashMap<Enchantment, Integer>();

		if(split.length > 1) {
			state = split[1];
			String[] split2 = state.split("!");
			state = split2[0];
			if (split2.length > 1) {
				
					String enchantment = split2[1];
					
					String[] split3 = enchantment.split("~");
					enchantment = split3[0];

					enchPass = CommonEnchantments.parseEnchantments(enchantment);
					//OtherDrops.logInfo(enchPass.keySet().toString());

					if (split3.length > 1) {
						loreName = split3[1];
					}
				
			}
		}

		Material mat = null;
		try {
			int dropInt = Integer.parseInt(drop);
			mat = Material.getMaterial(dropInt);
		} catch(NumberFormatException e) {
			mat = CommonMaterial.matchMaterial(drop);
		}
		if (mat == null) return null;


		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ItemDrop(amount, mat, d, chance, enchPass);
		} catch(NumberFormatException e) {}
		Data data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			Log.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data, chance, enchPass, loreName);
		return new ItemDrop(amount, mat, chance, enchPass, loreName);
	}

	@Override
	public String getName() {
		if (material == null) return "DEFAULT";
		String ret = material.toString();
		// TODO: Will durability ever be null, or will it just be 0?
		if(durability != null) {
			String dataString = durability.get(material);
			ret += (dataString.isEmpty()) ? "" : "@" + durability.get(material);
		}
		return ret;
	}

	@Override
	public double getAmount() {
		return rolledQuantity;
	}

	@Override
	public DoubleRange getAmountRange() {
		return quantity.toDoubleRange();
	}

	public Material getMaterial() {
		return material;
	}
}
