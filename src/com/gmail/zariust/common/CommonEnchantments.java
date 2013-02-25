package com.gmail.zariust.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.options.IntRange;

public class CommonEnchantments {
  // aliases
	
	public static List<CMEnchantment> parseEnchantments(String enchantments) {
		List<CMEnchantment> enchList = new ArrayList<CMEnchantment>();

		if(!enchantments.isEmpty()) {
			String[] split3 = enchantments.split("!");
			Log.logInfo("CommonEnch: processing enchantment: "+enchantments, Verbosity.HIGHEST);
			for (String loopEnchantment : split3) {
				CMEnchantment cmEnch = parseFromString(loopEnchantment);
				if (cmEnch != null) enchList.add(cmEnch);
			}
		}
		
		return enchList;
	}

	/**
	 * @param input
	 * @return
	 */
	private static CMEnchantment parseFromString(String input) {
		String[] enchSplit = input.split("#");
		String enchString = enchSplit[0].trim().toLowerCase();

		String enchLevel = "";
		if (enchSplit.length > 1) enchLevel = enchSplit[1];
		IntRange enchLevelInt = null;

		try {
			if (!enchLevel.isEmpty() && enchLevel.matches("[0-9-]*"))
				enchLevelInt = IntRange.parse(enchLevel);
		} catch(NumberFormatException x) {
			// do nothing - default enchLevelInt of 1 is fine (the drop itself will set this to ench.getStartLevel())
			enchLevelInt = null;
		}

		// Aliases
		enchString = enchString.replaceAll("[ _-]", "");

		Map <String, String> aliases = new HashMap<String, String>();
		aliases.put("aspectfire", "fire_aspect");
		aliases.put("sharpness", "damage_all");
		aliases.put("smite", "damage_undead");
		aliases.put("punch", "arrow_knockback");
		aliases.put("looting", "loot_bonus_mobs");
		aliases.put("fortune", "loot_bonus_blocks");
		aliases.put("baneofarthropods", "damage_undead");
		aliases.put("power", "arrow_damage");
		aliases.put("flame", "arrow_fire");
		aliases.put("infinity", "arrow_infinite");
		aliases.put("unbreaking", "durability");
		aliases.put("efficiency", "dig_speed");
		aliases.put("smite", "damage_undead");

		if (aliases.get(enchString) != null)
			enchString = aliases.get(enchString);

		enchString = enchString.replaceAll("[ _-]", ""); // once more for good measure :)


		Enchantment ench = null;

		for (Enchantment value : Enchantment.values()) {
			if (enchString.equalsIgnoreCase(value.getName().replaceAll("[ _-]", ""))) {
				ench = value;
			}
		}

		if (ench == null && !enchString.equalsIgnoreCase("random")) {
			Log.logInfo("Enchantment ("+input+"=>"+enchString+") not valid.", Verbosity.NORMAL);										
			return null;
		}

		if (ench != null) {
			if (enchLevelInt == null && !enchLevel.equals("?")) {
				enchLevelInt = IntRange.parse("1");

				if (!OtherDropsConfig.enchantmentsIgnoreLevel) {
					if (enchLevelInt.getMin() < ench.getStartLevel()) enchLevelInt.setMin(ench.getStartLevel());
					else if (enchLevelInt.getMax() > ench.getMaxLevel()) enchLevelInt.setMax(ench.getMaxLevel());
				}
			}
		}

		CMEnchantment cmEnch = new CMEnchantment();
		cmEnch.setEnch(ench);
		
		if (enchLevel.equals("?")) cmEnch.setLevelRange(null);
		else cmEnch.setLevelRange(enchLevelInt);

		return cmEnch;
	}

	public static boolean containsEnchantment(String enchantments, List<String>enchList) {
		return false;
	}

	public static ItemStack applyEnchantments(ItemStack stack, List<CMEnchantment> enchantments) {
		if (enchantments == null) return stack;
		
		if (!(enchantments.isEmpty())) {
			for (CMEnchantment cmEnch : enchantments) {
				Enchantment ench = cmEnch.getEnch(stack);
				int level = cmEnch.getLevel();
				
				try {
					if (OtherDropsConfig.enchantmentsUseUnsafe) {
						stack.addUnsafeEnchantment(ench, level);
					} else {
						stack.addEnchantment(ench, level);
					}
					Log.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" applied.", Verbosity.HIGHEST);
				} catch (IllegalArgumentException ex) {
					Log.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" cannot be applied ("+ex.getMessage()+").", Verbosity.HIGHEST);
				}
			}
		}
		return stack;
	}

	/**
	 * @param stack
	 * @return
	 */
	public static Enchantment getRandomEnchantment(ItemStack stack) {
		Enchantment ench;
		int length = Enchantment.values().length;
		ench = Enchantment.values()[OtherDrops.rng.nextInt(length-1)];
		int count = 0;
		if (!OtherDropsConfig.enchantmentsUseUnsafe) {
			while ((stack == null || !ench.canEnchantItem(stack)) && count < 50) {
				ench = Enchantment.values()[OtherDrops.rng.nextInt(length-1)];
				count++;  // try only a limited number of times
			}
		}
		return ench;
	}

	// eg. damage_all, d_arach   =   d_arach, damage_all
	public static boolean matches(List<CMEnchantment> customEnchs, Map<Enchantment, Integer> toolEnchs) {
		int matchCount = 0;
		for (CMEnchantment ench: customEnchs) {
			for (Entry<Enchantment, Integer> entry : toolEnchs.entrySet()) {
				if (ench.getEnchRaw() != null) if (ench.getEnchRaw() == entry.getKey()) matchCount++;
				
				if (ench.getLevelRange().contains(entry.getValue())) matchCount++;
			}
		}
		
		if ((matchCount/2) != customEnchs.size()) return false;
		
		return true;
	}
	
}
