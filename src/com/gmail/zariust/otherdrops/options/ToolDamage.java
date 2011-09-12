package com.gmail.zariust.otherdrops.options;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.ConfigurationNode;

public class ToolDamage {
	private ShortRange durabilityRange;
	private IntRange consumeRange;
	private Material replace;
	
	public boolean apply(ItemStack stack) {
		boolean fullyConsumed = false;
		short maxDurability = stack.getType().getMaxDurability();
		if(maxDurability > 0 && durabilityRange != null) {
			short durability = stack.getDurability();
			short damage = durabilityRange.getRandomIn();
			if(durability + damage >= maxDurability) fullyConsumed = true;
			else stack.setDurability((short) (durability + damage));
		}
		if(consumeRange != null && (fullyConsumed || durabilityRange == null)) {
			if(fullyConsumed) {
				fullyConsumed = false;
				stack.setDurability((short)0);
			}
			int count = stack.getAmount();
			int take = consumeRange.getRandomIn();
			if(count <= take) fullyConsumed = true;
			else stack.setAmount(count - take);
		}
		if(replace != null && fullyConsumed) {
			fullyConsumed = false;
			stack.setDurability((short)0);
			stack.setAmount(1);
			stack.setType(replace);
		} else if(durabilityRange == null && consumeRange == null) {
			fullyConsumed = false;
			stack.setDurability((short)0);
			stack.setType(replace);
		}
		return fullyConsumed;
	}

	public static ToolDamage parseFrom(ConfigurationNode node) {
		ToolDamage damage = new ToolDamage();
		// Durability
		String durability = node.getString("damagetool");
		if(durability != null) damage.durabilityRange = ShortRange.parse(durability);
		else {
			durability = node.getString("fixtool");
			if(durability != null) {
				ShortRange range = ShortRange.parse(durability);
				damage.durabilityRange = range.negate(range);
			}
		}
		// Amount
		String consume = node.getString("consumetool");
		if(consume != null) damage.consumeRange = IntRange.parse(consume);
		else {
			consume = node.getString("growtool");
			if(consume != null) {
				IntRange range = IntRange.parse(consume);
				damage.consumeRange = range.negate(range);
			}
		}
		// Replace
		String replace = node.getString("replacetool");
		if(replace != null) damage.replace = Material.getMaterial(replace);
		if(damage.durabilityRange != null || damage.consumeRange != null || damage.replace != null)
			return damage;
		return null;
	}
}
