package com.gmail.zariust.bukkit.otherblocks.options;

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
import org.bukkit.material.MaterialData;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP};

	private DropCategory cat;
	private ItemStack item;
	private CreatureType mob;
	private int mobData;
	private double loot;
	private double chance;

	public DropType(DropCategory type) {
		cat = type;
	}
	
	// Item drops
	public DropType(Material mat) {
		this(mat, 100.0);
	}
	
	public DropType(MaterialData mat) {
		this(mat, 100.0);
	}
	
	public DropType(Material mat, int amount) {
		this(mat, amount, 100.0);
	}

	public DropType(MaterialData mat, int amount) {
		this(mat, amount, 100.0);
	}

	public DropType(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public DropType(Material mat, double percent) {
		this(mat.getNewData((byte) 0), percent);
	}
	
	public DropType(MaterialData mat, double percent) {
		this(mat.toItemStack(), percent);
	}
	
	public DropType(Material mat, int amount, double percent) {
		this(mat.getNewData((byte) 0), amount, percent);
	}
	
	public DropType(MaterialData mat, int amount, double percent) {
		this(mat.toItemStack(amount), percent);
	}
	
	public DropType(ItemStack stack, double percent) {
		this(DropCategory.ITEM);
		item = stack;
		chance = percent;
	}
	
	// Creature drops
	public DropType(CreatureType type) {
		this(type, 0);
	}
	
	public DropType(CreatureType type, int data) {
		this(DropCategory.CREATURE);
		mob = type;
		mobData = data;
	}
	
	public DropType(Entity e) {
		this(CommonEntity.getCreatureType(e), CommonEntity.getCreatureData(e));
	}
	
	// Money drops
	public DropType(double money) {
		this(money, 100.0);
	}
	
	public DropType(double money, double percent) {
		this(DropCategory.MONEY);
		loot = money;
		chance = percent;
	}
	
	// Accessors
	public DropCategory getCategory() {
		return cat;
	}

	public ItemStack getItem() {
		return item;
	}

	public CreatureType getCreature() {
		return mob;
	}

	public int getCreatureData() {
		return mobData;
	}

	public double getMoney() {
		return loot;
	}

	public double getChance() {
		return chance;
	}
	
	// Drop now!
	public void drop(Location where, Player recipient, boolean naturally, boolean spread) {
		if(chance < 100.0) {
			// TODO: Check chance, and break if fails
			// if(rng.nextDouble() * 100 < chance) return;
		}
		switch(cat) {
		case ITEM:
			if(spread) {
				int count = item.getAmount();
				item.setAmount(1);
				while(count-- > 0) drop(where, item, naturally);
			} else drop(where, item, naturally);
			break;
		case CREATURE:
			drop(where, recipient, mob, mobData);
			break;
		case MONEY:
			drop(recipient, loot);
			break;
		case GROUP:
			// TODO: Drop groups
		}
	}
	
	private static void drop(Location where, ItemStack stack, boolean naturally) {
		World in = where.getWorld();
		if(naturally) in.dropItemNaturally(where, stack);
		else in.dropItem(where, stack);
	}
	
	private static void drop(Location where, AnimalTamer owner, CreatureType type, int data) {
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
	
	private static void drop(Player recipient, double money) {
		OtherBlocks.method.getAccount(recipient.getName()).add(money);
	}
}
