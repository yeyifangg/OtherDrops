package com.gmail.zariust.otherdrops.data.itemmeta;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class OdEnchantedBookMeta  extends OdItemMeta {
	public String enchantment;
	
	public OdEnchantedBookMeta(String owner) {
		this.enchantment = owner;
	}

	public ItemStack setOn(ItemStack stack) {
		if (enchantment == null) return null;
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
		meta.addStoredEnchant(Enchantment.getByName(enchantment), 1, true);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static OdItemMeta parse(String sub) {
		if (!sub.isEmpty() && Enchantment.getByName(sub) != null) {
			return new OdEnchantedBookMeta(sub);
		} else {
			return null;
		}
	}
}
