package com.gmail.zariust.otherdrops.drop;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.subject.LivingSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;

public abstract class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT, VEHICLE, EXPERIENCE};
	public static class DropFlags {
		protected boolean naturally, spread;
		protected Random rng;
		protected Player recipient, victim;
		protected Entity entity;
		
		protected DropFlags(boolean n, boolean s, Entity c, Random ran, Player who, Player dead) {
			naturally = n;
			spread = s;
			entity = c;
			rng = ran;
			recipient = who;
			victim = dead;
		}
	};

	private DropCategory cat;
	private double chance;
	protected Location offsetLocation;

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
	
	public static DropFlags flags(Player recipient, Player victim, boolean naturally, boolean spread, Target creature, Random rng) {
		Entity entity = null;
		if(creature instanceof LivingSubject) entity = ((LivingSubject)creature).getEntity();
		else if(creature instanceof VehicleTarget) entity = ((VehicleTarget)creature).getVehicle();
		return new DropFlags(naturally, spread, entity, rng, recipient, victim);
	}
	
	// Drop now!
	public void drop(Location from, Location offset, double amount, DropFlags flags) {
		offset.setWorld(from.getWorld()); // To avoid "differing world" errors
		this.offsetLocation = from.clone().add(offset);
		drop(from, amount, flags);
	}
	
	public void drop(Location from, double amount, DropFlags flags) {
		if(chance < 100.0) {
			if(flags.rng.nextDouble() <= chance / 100.0) return;
		}
		int quantity = calculateQuantity(amount);
		if(quantity == 0) return;
		while(quantity-- > 0) performDrop(from, flags);
	}
	
	// Methods to override!
	protected abstract void performDrop(Location from, DropFlags flags);
	
	public abstract double getAmount();
	
	@Override
	public abstract String toString();
	
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
		} else if(isCreature(name)) return CreatureDrop.parse(name, defaultData, (int) amount, chance);
		else if(name.startsWith("VEHICLE_")) return VehicleDrop.parse(name, defaultData, (int) amount, chance);
		else if(name.startsWith("MONEY")) return MoneyDrop.parse(name, defaultData, amount, chance);
		else if(name.startsWith("XP")) return ExperienceDrop.parse(name, defaultData, (int) amount, chance);
		else if(name.equals("CONTENTS")) return new ContentsDrop();
		else if(name.equals("DEFAULT")) return null;
		else if(name.equals("THIS")) return new SelfDrop((int) amount, chance);
		return ItemDrop.parse(name, defaultData, (int) amount, chance);
	}

	// TODO: put this in a better location - duplicated code, also used in OtherDrops config
	public static boolean isCreature(String name) {
		if (name.startsWith("CREATURE_")) return true;
		name = name.split("@")[0];
		try {
			if (CreatureType.valueOf(name) != null) return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
		
		return false;
	}

	public boolean isQuantityInteger() {
		return true;
	}
}
