package com.gmail.zariust.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.Log;

public class CommonEnchantments {
  // aliases
	
	public static Map<Enchantment, Integer> parseEnchantments(String enchantments) {
		Map <Enchantment, Integer> enchList = new HashMap<Enchantment, Integer>();

		if(enchantments != "") { 
			String[] split3 = enchantments.split(",");
			Log.logInfo("CommonEnch: processing enchantment: "+enchantments, Verbosity.HIGHEST);
			for (String loopEnchantment : split3) {
				String[] enchSplit = loopEnchantment.split("#");
				String enchantment = enchSplit[0].trim().toLowerCase();

				String enchLevel = "";
				if (enchSplit.length > 1) enchLevel = enchSplit[1];
				Integer enchLevelInt = 1;

				try {
					enchLevelInt = Integer.parseInt(enchLevel);
				} catch(NumberFormatException x) {
					// do nothing - default enchLevelInt of 1 is fine (the drop itself will set this to ench.getStartLevel())
				}

				// Aliases
				enchantment = enchantment.replaceAll("[ _-]", "");

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

				if (aliases.get(enchantment) != null)
					enchantment = aliases.get(enchantment);
				
				enchantment = enchantment.replaceAll("[ _-]", ""); // once more for good measure :)

				Enchantment ench = null;
				for (Enchantment value : Enchantment.values()) {
					if (enchantment.equalsIgnoreCase(value.getName().replaceAll("[ _-]", ""))) {
						ench = value;
					}
				}
				
				if (ench != null) {
					if (enchLevelInt < ench.getStartLevel()) enchLevelInt = ench.getStartLevel();
					else if (enchLevelInt > ench.getMaxLevel()) enchLevelInt = ench.getMaxLevel();
					
					enchList.put(ench, enchLevelInt);
				} else {
					Log.logInfo("Enchantment ("+loopEnchantment+"=>"+enchantment+") not valid.", Verbosity.NORMAL);					
				}
			}
		}
		
		return enchList;
	}
  
	public static boolean containsEnchantment(String enchantments, List<String>enchList) {
		return false;
	}

	public static ItemStack applyEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
		
		if (!(enchantments.isEmpty())) {
			for (Enchantment ench : enchantments.keySet()) {
					int level = enchantments.get(ench);
					try {
						stack.addEnchantment(ench, level);
						Log.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" applied.", Verbosity.HIGHEST);
					} catch (IllegalArgumentException ex) {
						Log.logInfo("Enchantment ("+ench.getStartLevel()+"-"+ench.getMaxLevel()+"): "+ench.getName()+"#"+level+" cannot be applied ("+ex.getMessage()+").", Verbosity.HIGHEST);
					}
			}
		}
		return stack;
	}

	public static boolean matches(Map<Enchantment, Integer> customEnchs,
			Map<Enchantment, Integer> toolEnchs) {
		// TODO Auto-generated method stub
		for (Enchantment ench: customEnchs.keySet()) {
			if (!toolEnchs.keySet().contains(ench)) return false;
		}

		return true;
	}
	
}
