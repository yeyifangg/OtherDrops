package com.gmail.zariust.bukkit.otherblocks.options.drop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.common.MaterialGroup;

public abstract class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT, VEHICLE};
	protected static class DropFlags {
		public boolean naturally;
		public boolean spread;
		public Random rng;
		public Player recipient;
		
		public DropFlags(boolean n, boolean s, Random ran, Player who) {
			naturally = n;
			spread = s;
			rng = ran;
			recipient = who;
		}
	};

	private DropCategory cat;
	private double chance;

	public DropType(DropCategory type) {
		this(type, 100.0);
	}

	public DropType(DropCategory type, double percent) {
		cat = type;
		chance = percent;
	}
	
	// Accessors
	public DropCategory getCategory() {
		return cat;
	}

	public double getChance() {
		return chance;
	}
	
	// Drop now!
	public void drop(Location where, double amount, Player recipient, boolean naturally, boolean spread, Random rng) {
		if(chance < 100.0) {
			if(rng.nextDouble() <= chance / 100.0) return;
		}
		drop(where, amount, new DropFlags(naturally, spread, rng, recipient));
	}
	
	public void drop(Location where, double amount, DropFlags flags) {
		int quantity = calculateQuantity(amount);
		while(quantity-- > 0)
			performDrop(where, flags);
	}
	
	// Methods to override!
	protected abstract void performDrop(Location where, DropFlags flags);
	
	protected int calculateQuantity(double amount) {
		int intPart = (int) amount;
		// (int) discards the decimal place - round up if neccessary
		if (amount - intPart >= 0.5)
			intPart = intPart + 1;
		return intPart;
	}
	
	// Drop an item!
	protected static void drop(Location where, ItemStack stack, boolean naturally) {
		if(stack.getType() == Material.AIR) return; // don't want to crash clients with air item entities
		World in = where.getWorld();
		if(naturally) in.dropItemNaturally(where, stack);
		else in.dropItem(where, stack);
	}
	
	// Drop a creature!
	protected static void drop(Location where, AnimalTamer owner, CreatureType type, int data) {
		World in = where.getWorld();
		LivingEntity mob = in.spawnCreature(where, type);
		switch(type) {
		case CREEPER:
			if(data == 1) ((Creeper)mob).setPowered(true);
			break;
		case PIG:
			if(data == 1) ((Pig)mob).setSaddle(true);
			break;
		case SHEEP:
			if(data >= 16) ((Sheep)mob).setSheared(true);
			data -= 16;
			((Sheep)mob).setColor(DyeColor.getByData((byte) data));
			break;
		case SLIME:
			if(data > 0) ((Slime)mob).setSize(data);
			break;
		case WOLF:
			switch(data) {
			case 1:
				((Wolf)mob).setAngry(true);
				break;
			case 2:
				((Wolf)mob).setTamed(true);
				((Wolf)mob).setOwner(owner);
				break;
			}
			break;
		case PIG_ZOMBIE:
			if(data > 0) ((PigZombie)mob).setAnger(data);
			break;
		default:
		}
	}

	@SuppressWarnings("rawtypes")
	public static DropType parseFrom(ConfigurationNode node) {
		Object drop = node.getProperty("drop");
		String colour = node.getString("color");
		if(colour == null) colour = node.getString("colour");
		if(colour == null) colour = node.getString("data");
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
		double amount = 1;
		try {
			amount = Double.parseDouble(split[1]);
		} catch(NumberFormatException e) {}
		double chance = 100.0;
		try {
			chance = Double.parseDouble(split[2]);
		} catch(NumberFormatException e) {}
		// Drop can be one of the following
		// - A Material constant, or one of the synonyms NOTHING and DYE
		// - A Material constant prefixed with VEHICLE_
		// - A CreatureType constant prefixed with CREATURE_
		// - A MaterialGroup constant beginning with ANY_, optionally prefixed with ^
		// - One of the special keywords DEFAULT, DENY, MONEY, CONTENTS
		if(name.startsWith("ANY_")) {
			return ExclusiveDropGroup.parse(drop, defaultData, (int) amount, chance);
		} else if(name.startsWith("^ANY_")) {
			return SimpleDropGroup.parse(drop, defaultData, (int) amount, chance);
		} else if(name.startsWith("CREATURE_")) return CreatureDrop.parse(drop, defaultData, (int) amount, chance);
		else if(name.startsWith("VEHICLE_")) return VehicleDrop.parse(drop, defaultData, (int) amount, chance);
		else if(name.equals("DEFAULT")) return new DefaultDrop();
		else if(name.equals("DENY")) return new DenyDrop();
		else if(name.startsWith("MONEY")) return MoneyDrop.parse(drop, defaultData, amount, chance);
		else if(name.equals("CONTENTS")) return new ContentsDrop();
		return ItemDrop.parse(drop, defaultData, (int) amount, chance);
	}
}
