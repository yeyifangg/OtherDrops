package com.gmail.zariust.bukkit.otherblocks.options.drop;

import java.util.Random;

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

public abstract class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT};

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
		drop(where, amount, recipient, naturally, spread);
	}
	
	public void drop(Location where, double amount, Player recipient, boolean naturally, boolean spread) {
		int quantity = calculateQuantity(amount);
		while(quantity-- > 0)
			performDrop(where, recipient, naturally, spread);
	}
	
	// Methods to override!
	protected abstract void performDrop(Location where, Player recipient, boolean naturally, boolean spread);
	
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
}
