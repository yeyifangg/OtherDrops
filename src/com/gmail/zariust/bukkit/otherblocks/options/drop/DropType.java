package com.gmail.zariust.bukkit.otherblocks.options;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.CreatureSpawner;
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
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public class DropType {
	public enum DropCategory {ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT};

	private DropCategory cat;
	private ItemStack item;
	private CreatureType mob;
	private int mobData;
	private double loot;
	private double chance;
	private List<DropType> group;
	private Cancellable event;

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
	
	// Simple drop group
	public DropType(DropType... drops) {
		this(DropCategory.GROUP);
		group = Arrays.asList(drops);
	}
	
	// Deny
	public DropType(Cancellable evt) {
		this(DropCategory.DENY);
		event = evt;
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
	
	public List<DropType> getGroup() {
		return group;
	}
	
	// Drop now!
	public void drop(Location where, double amount, Player recipient, boolean naturally, boolean spread) {
		if(chance < 100.0) {
			// TODO: Check chance, and break if fails
			// if(rng.nextDouble() * 100 < chance) return;
		}
		int quantity;
		if(cat == DropCategory.MONEY) {
			quantity = 1;
			loot *= amount;
		} else quantity = (int) amount;
		while(quantity-- > 0) {
			switch(cat) {
			case ITEM:
				if(spread) {
					int count = item.getAmount();
					item.setAmount(1);
					while(count-- > 0) drop(where, item, naturally);
				} else drop(where, item, naturally);
				break;
			case CREATURE:
				// TODO: Honour embedded quantity
				drop(where, recipient, mob, mobData);
				break;
			case MONEY:
				drop(recipient, loot);
				break;
			case GROUP:
				for(DropType drop : group) drop.drop(where, quantity, recipient, naturally, spread);
				break;
			case DENY:
				event.setCancelled(true);
				break;
			case CONTENTS:
				dropContents(where, naturally);
				break;
			case DEFAULT:
				// Do nothing; TODO This probably won't work quite as expected.
				break;
			}
		}
	}
	
	private static void drop(Location where, ItemStack stack, boolean naturally) {
		if(stack.getType() == Material.AIR) return; // don't want to crash clients with air item entities
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
	
	private static void drop(Location where, Inventory container, boolean naturally) {
		for(ItemStack item : container.getContents()) drop(where, item, naturally);
	}
	
	private static void dropContents(Location where, boolean naturally) {
		// First locate the object; it's a block, storage minecart, or player
		Inventory container = null;
		Block block = where.getWorld().getBlockAt(where);
		BlockState state = block.getState();
		if(state instanceof ContainerBlock) {
			container = ((ContainerBlock) state).getInventory();
			// If it's a furnace which is smelting, remove one of what's being smelted.
		} /* else if(state instanceof Jukebox) { // Drop the currently playing record; commented out due to missing BlockState class
			Material mat = ((Jukebox) state).getPlaying();
			if(mat != null) drop(where, new ItemStack(mat, 1), naturally);
			return;
		} */ else if(state instanceof CreatureSpawner) { // Drop the creature in the spawner
			drop(where, null, ((CreatureSpawner) state).getCreatureType(), 0);
			return;
		} else { // It's not a container block, so it must be an entity
			List<Entity> entities = where.getWorld().getEntities();
			boolean found = false;
			for(Entity entity : entities) {
				// TODO: Is it really the case that the location will be identical in this case?
				// Shouldn't we check just block location to be sure?
				if(!entity.getLocation().equals(where)) continue;
				if(entity instanceof Player) {
					container = ((Player) entity).getInventory();
					found = true;
				} else if(entity instanceof StorageMinecart) {
					container = ((StorageMinecart) entity).getInventory();
					found = true;
				}
			}
			if(!found) {
				// TODO: Print error message?
				return;
			}
		}
		if(container == null) return; // Doubt this'll ever happen, but just in case
		// And now pass it on!
		drop(where, container, naturally);
	}
}
