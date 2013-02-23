package com.gmail.zariust.otherdrops.data.itemmeta;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class OdSkullMeta  extends OdItemMeta {
	public String owner;
	
	public OdSkullMeta(String owner) {
		this.owner = owner;
	}

	public ItemStack setOn(ItemStack stack) {
		if (owner == null) return null;
		SkullMeta meta = (SkullMeta) stack.getItemMeta();
		meta.setOwner(owner);
		stack.setDurability((short)3);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static OdItemMeta parse(String sub) {
		if (!sub.isEmpty()) {
			return new OdSkullMeta(sub);
		} else {
			return null;
		}
	}
}
