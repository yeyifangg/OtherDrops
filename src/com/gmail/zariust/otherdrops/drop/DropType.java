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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.LivingSubject;
import com.gmail.zariust.otherdrops.subject.Target;

public abstract class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT, VEHICLE, EXPERIENCE};
	public static class DropFlags {
		protected boolean naturally, spread;
		protected Random rng;
		protected Player recipient;
		
		protected DropFlags(boolean n, boolean s, Random ran, Player who) {
			naturally = n;
			spread = s;
			rng = ran;
			recipient = who;
		}
	};

	public static Entity actuallyDropped;
	public boolean overrideDefault;
	private DropCategory cat;
	private double chance;
	// For MoneyDrop: Without this separate total, the amount dropped would increase every time if there is both
	// an embedded quantity and an external quantity.
	// Moved into DropType as we need to make it available for messages
	public double total;
	protected String loreName;

	
	public DropType(DropCategory type) {
		this(type, 100.0);
	}

	public DropType(DropCategory type, double percent) {
		cat = type;
		chance = percent;
		actuallyDropped = null;
	}
	
	// Accessors
	public DropCategory getCategory() {
		return cat;
	}

	public double getChance() {
		return chance;
	}
	
	public static DropFlags flags(Player recipient, boolean naturally, boolean spread, Random rng) {
		return new DropFlags(naturally, spread, rng, recipient);
	}
	
	// Drop now! Return false if the roll fails
	public int drop(Location from, Target target, Location offset, double amount, DropFlags flags, OccurredEvent occurrence) {
		return drop(from, target, offset, amount, flags, true, occurrence, false);
	}
	
	protected int drop(Location from, Target target, Location loc, double amount, DropFlags flags, boolean offset, OccurredEvent occurrence, boolean fromExclusiveDrop) {
		Location offsetLocation;
		if(offset) {
			//Location from = target.getLocation();
			loc.setWorld(from.getWorld()); // To avoid "differing world" errors
			offsetLocation = from.clone().add(loc);
		} else offsetLocation = loc.clone();

		// note: exclusivedrop is a "chance distribution" and chance values have already been checked, so skip here if exclusivedrop
		if(chance < 100.0 && !fromExclusiveDrop) {
			double rolledChance = flags.rng.nextDouble();
			Log.logInfo("Rolling chance: checking "+rolledChance+" <= "+(chance/100)+" ("+(!(rolledChance > chance / 100.0))+")", Verbosity.HIGHEST);
			if(rolledChance > chance / 100.0) {
				Log.logInfo("Failed roll, returning...", Verbosity.HIGHEST);
				return -1;
			}
		}
		int quantity = calculateQuantity(amount, flags.rng);
		int actuallyDropped = 0;
		//OtherDrops.logInfo("Calling performDrop...",Verbosity.HIGHEST);
		for (int i=0;i<quantity;i++) {
			actuallyDropped += performDrop(target, offsetLocation, flags, occurrence);
		}
		return actuallyDropped;
	}
	
	// Methods to override!
	protected abstract int performDrop(Target source, Location at, DropFlags flags, OccurredEvent occurrence);
	public abstract double getAmount();
	public abstract DoubleRange getAmountRange();
	protected abstract String getName();
	
	@Override
	public final String toString() {
		String result = getName();
		DoubleRange amount = getAmountRange();
		if(amount.getMin() != 1 || amount.getMax() != 1)
			result += "/" + (isQuantityInteger() ? amount.toIntRange() : amount);
		if(chance < 100 || chance > 100)
			result += "/" + chance + "%";
		return result;
	}
	
	@SuppressWarnings("unused")
	protected int calculateQuantity(double amount, Random rng) {
		int intPart = (int) amount;
		// (int) discards the decimal place - round up if neccessary
		if (amount - intPart >= 0.5)
			intPart = intPart + 1;
		return intPart;
	}
	
	// Drop an item!
	protected static int drop(Location where, ItemStack stack, boolean naturally) {
		if(stack.getType() == Material.AIR) return 1; // don't want to crash clients with air item entities
		World in = where.getWorld();
		if(naturally) actuallyDropped = in.dropItemNaturally(where, stack);
		else actuallyDropped = in.dropItem(where, stack);
		return 1;
	}
	
	// Drop a creature!
	protected static int drop(Location where, Player owner, EntityType type, Data data) {
		World in = where.getWorld();
		LivingEntity mob;
		try {
			mob = in.spawnCreature(where, type);
			data.setOn(mob, owner);
			actuallyDropped = mob;
		} catch (Exception e) {
			Log.logInfo("DropType: failed to spawn entity '"+type.getName()+"' ("+e.getLocalizedMessage()+")", Verbosity.HIGH);
			//e.printStackTrace();
		}
		return 1;
	}

	@SuppressWarnings("rawtypes")
	public static DropType parseFrom(ConfigurationNode node) {
		Object drop = node.get("drop");
		String colour = OtherDropsConfig.getStringFrom(node, "color", "colour", "data");
		if(colour == null) colour = "0";
		if(drop == null) return null;
		else if(drop instanceof List) {
			List<String> dropList = new ArrayList<String>();
			for(Object obj : (List)drop) dropList.add(obj.toString());
			return SimpleDropGroup.parse(dropList, colour);
		} else if(drop instanceof Map) {
			List<String> dropList = new ArrayList<String>();
			for(Object obj : ((Map)drop).keySet()) dropList.add(obj.toString());
			return ExclusiveDropGroup.parse(dropList, colour);
		} else if(drop instanceof Set) { // Probably'll never happen, but whatever
			List<String> dropList = new ArrayList<String>();
			for(Object obj : ((Set)drop)) dropList.add(obj.toString());
			return ExclusiveDropGroup.parse(dropList, colour);
		} else return parse(drop.toString(), colour);
	}
	
	private static String[] split(String drop) {
		String name, amount, chance;
		String[] split = drop.split("/");
		switch(split.length){
		case 3:
			if(split[1].endsWith("%")) {
				chance = split[1].substring(0, split[1].length() - 1);
				amount = split[2];
			} else {
				chance = split[2].substring(0, split[2].length() - 1);
				amount = split[1];
			}
			break;
		case 2:
			if(split[1].endsWith("%")) {
				chance = split[1].substring(0, split[1].length() - 1);
				amount = "";
			} else {
				chance = "";
				amount = split[1];
			}
			break;
		default:
			chance = amount = "";
		}
		name = split[0];
		return new String[] {name, amount, chance};
	}

	public static DropType parse(String drop, String defaultData) {
		String[] split = split(drop);
		String name = split[0].toUpperCase();
		DoubleRange amount = new DoubleRange(1.0,1.0);
		try {
			amount = DoubleRange.parse(split[1]);
		} catch(IllegalArgumentException e) {
			amount = new DoubleRange(1.0,1.0);
		}
		double chance = 100.0;
		try {
			chance = Double.parseDouble(split[2]);
		} catch(NumberFormatException e) {
			chance = 100.0;
		}
		// Drop can be one of the following
		// - A Material constant, or one of the synonyms NOTHING and DYE
		// - A Material constant prefixed with VEHICLE_
		// - A EntityType constant prefixed with CREATURE_
		// - A MaterialGroup constant beginning with ANY_, optionally prefixed with ^ to indicate ALL
		// - One of the special keywords DEFAULT, DENY, MONEY, CONTENTS
		if(name.startsWith("ANY_")) {
			return ExclusiveDropGroup.parse(drop, defaultData, amount.toIntRange(), chance);
		} else if(name.startsWith("^ANY_") || name.startsWith("EVERY_")) {
			return SimpleDropGroup.parse(drop, defaultData, amount.toIntRange(), chance);
		} else {
			DropType dropType = CreatureDrop.parse(name, defaultData, amount.toIntRange(), chance);
			if (dropType != null) return dropType;
			else if(name.startsWith("VEHICLE_")) return VehicleDrop.parse(name, defaultData, amount.toIntRange(), chance);
			else if(name.startsWith("MONEY")) return MoneyDrop.parse(name, defaultData, amount, chance);
			else if(name.startsWith("XP")) return ExperienceDrop.parse(name, defaultData, amount.toIntRange(), chance);
			else if(name.equals("CONTENTS")) return new ContentsDrop();
			else if(name.equals("DEFAULT")) return new ItemDrop((Material)null);
			else if(name.equals("THIS") || name.equals("SELF")) return new SelfDrop(amount.toIntRange(), chance);
			return ItemDrop.parse(name, defaultData, amount.toIntRange(), chance);
		}
	}

	public boolean isQuantityInteger() {
		return true;
	}

	public String getLoreName() {
		return loreName;
	}

	public void setLoreName(String msg) {
		this.loreName = msg;
		
	}
}
