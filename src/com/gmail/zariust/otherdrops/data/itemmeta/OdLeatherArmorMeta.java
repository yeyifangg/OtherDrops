package com.gmail.zariust.otherdrops.data.itemmeta;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;

public class OdLeatherArmorMeta extends OdItemMeta {
	public Color color;
	
	public OdLeatherArmorMeta(Color color2) {
		this.color = color2;
	}

	public ItemStack setOn(ItemStack stack) {
		Log.dMsg("set on");
		if (color != null) {
			LeatherArmorMeta lam = (LeatherArmorMeta)stack.getItemMeta();
			lam.setColor(color);
			stack.setItemMeta(lam);
		}
		return stack;
	}


	public static OdItemMeta parse(String sub) {
		Log.dMsg("parse leather meta");
		
		Color color = OdItemMeta.getColorFrom(sub.toUpperCase());
		if (color != null) {
			return new OdLeatherArmorMeta(color);
		} else {
			Log.logInfo("ItemDrop: error - leather armour color not valid.", Verbosity.NORMAL);
			return null;
		}
	}
}
