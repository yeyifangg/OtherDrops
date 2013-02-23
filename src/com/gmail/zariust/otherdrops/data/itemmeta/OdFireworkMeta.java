package com.gmail.zariust.otherdrops.data.itemmeta;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class OdFireworkMeta  extends OdItemMeta {
	public String owner;
	
	public OdFireworkMeta(String owner) {
		this.owner = owner;
	}

	public ItemStack setOn(ItemStack stack) {
		if (owner == null) return null;
		FireworkMeta meta = (FireworkMeta) stack.getItemMeta();
		// FIXME: allow for custom details
		Color color = OdItemMeta.getColorFrom(owner);
		if (color != null) {
			meta.addEffect(FireworkEffect.builder().trail(false).flicker(false).withColor(color).build());
		}
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static OdItemMeta parse(String sub) {
		if (!sub.isEmpty()) {
			return new OdFireworkMeta(sub);
		} else {
			return null;
		}
	}
}
