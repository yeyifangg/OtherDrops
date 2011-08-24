package com.gmail.zariust.bukkit.otherblocks.droptype;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ContentsDrop extends DropType {
	public ContentsDrop() {
		super(DropCategory.CONTENTS);
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		// First locate the object; it's a block, storage minecart, or player
		Inventory container = null;
		Block block = where.getWorld().getBlockAt(where);
		BlockState state = block.getState();
		if(state instanceof ContainerBlock) {
			container = ((ContainerBlock) state).getInventory();
			// If it's a furnace which is smelting, remove one of what's being smelted.
			// TODO: A way to give the user a choice whether this happens
			if(state instanceof Furnace) {
				Furnace oven = (Furnace) state;
				ItemStack cooking = container.getItem(0); // first item is the item being smelted
				if(oven.getCookTime() > 0) cooking.setAmount(cooking.getAmount()-1);
				if(cooking.getAmount() <= 0) container.setItem(0, null);
			}
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
				Location location = entity.getLocation();
				if(location.getBlockX() != where.getBlockX()) continue;
				if(location.getBlockY() != where.getBlockY()) continue;
				if(location.getBlockZ() != where.getBlockZ()) continue;
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
		drop(where, container, flags.naturally);
	}
	
	private static void drop(Location where, Inventory container, boolean naturally) {
		for(ItemStack item : container.getContents()) drop(where, item, naturally);
	}

	@Override
	public String toString() {
		return "CONTENTS";
	}
}
