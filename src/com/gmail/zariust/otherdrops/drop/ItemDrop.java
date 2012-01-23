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
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class ItemDrop extends DropType {
	private Material material;
	private Data durability;
	private IntRange quantity;
	private int rolledQuantity;
	private Map<String, Integer> enchantments;
	
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
		this(new ItemStack(mat, 1, (short) data), percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, double percent, Map<String, Integer> enchantment) {
		this(amount, mat, 0, percent, enchantment);
	}
	
	public ItemDrop(IntRange amount, Material mat, int data, double percent, Map<String, Integer> enchantment) {
		this(amount, mat, new ItemData(data), percent, enchantment);
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		this(new IntRange(stack.getAmount()), stack.getType(), new ItemData(stack), percent, null);
	}
	
	public ItemDrop(IntRange amount, Material mat, Data data, double percent, Map<String, Integer> enchPass) { // Rome
		super(DropCategory.ITEM, percent);
		quantity = amount;
		material = mat;
		durability = data;
		this.enchantments = enchPass;
	}

	public ItemStack getItem(Random rng) {
		rolledQuantity = quantity.getRandomIn(rng);
		return new ItemStack(material, rolledQuantity, (short)durability.getData());
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(quantity.getMax() == 0) return;
		if(flags.spread) {
			ItemStack stack = new ItemStack(material, 1, (short)durability.getData());
			if (enchantments != null) {
			if (!(enchantments.isEmpty())) {
				for (String enchName : enchantments.keySet()) {
					Enchantment ench = Enchantment.getByName(enchName);
					if (ench != null) {
						Integer level = enchantments.get(enchName); 
						if (level < ench.getStartLevel()) level = ench.getStartLevel();
						else if (level > ench.getMaxLevel()) level = ench.getMaxLevel();

						try {
							stack.addEnchantment(ench, enchantments.get(enchName));
							OtherDrops.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" applied.", Verbosity.HIGHEST);
						} catch (IllegalArgumentException ex) {
							OtherDrops.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" cannot be applied ("+ex.getMessage()+").", Verbosity.HIGHEST);
//							OtherDrops.logWarning("Dropping "+material.toString()+", but cannot add enchantment ("+ex.getMessage()+").", Verbosity.HIGH);
						}
					}
				}
			}
			}
			int count = quantity.getRandomIn(flags.rng);
			while(count-- > 0) drop(where, stack, flags.naturally);
		} else drop(where, getItem(flags.rng), flags.naturally);

	}

	public static DropType parse(String drop, String defaultData, IntRange amount, double chance) {
		drop = drop.toUpperCase();
		String state = defaultData;
		String[] split = drop.split("@");
		drop = split[0];

		Map <String, Integer> enchPass = new HashMap<String, Integer>();

		if(split.length > 1) { 
			state = split[1];
			
			String[] split2 = state.split("!");
			state = split2[0];
			if (split2.length > 1) {
				String[] split3 = split2[1].split(",");
				OtherDrops.logInfo("Processing enchantment: "+split2.toString(), Verbosity.HIGHEST);
				for (String enchantment : split3) {
					String[] enchSplit = enchantment.split("#");
					String enchLevel = "";
					enchantment = enchSplit[0];
					if (enchSplit.length > 1) enchLevel = enchSplit[1];
					Integer enchLevelInt = 1;
					try {
						enchLevelInt = Integer.parseInt(enchLevel);
					} catch(NumberFormatException x) {
						// do nothing - default enchLevelInt of 1 is fine (the drop itself will set this to ench.getStartLevel())
					}

					enchPass.put(enchSplit[0], enchLevelInt);
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
			OtherDrops.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data, chance, enchPass);
		return new ItemDrop(amount, mat, chance, enchPass);
	}

	@Override
	public String getName() {
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
