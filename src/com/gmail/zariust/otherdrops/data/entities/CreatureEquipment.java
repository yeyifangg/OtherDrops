package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.inventory.ItemStack;

class CreatureEquipment {
public class CreatureEquipment {
	public ItemStack head;
	public Float headChance = 10F;
	public ItemStack chest;
	public Float chestChance = 10F;
	public ItemStack legs;
	public Float legsChance = 10F;
	public ItemStack hands;
	public Float handsChance = 10F;
	public ItemStack boots;
	public Float bootsChance = 10F;
	
	public String toString() {
		String msg = "";
		
		if (head != null)
			msg += "!!" + head.toString() + "%" + headChance.toString() + "%";
		if (chest != null)
			msg += "!!" + chest.toString() + "%" + chestChance.toString() + "%";
		if (legs != null)
			msg += "!!" + legs.toString() + "%" + legsChance.toString() + "%";
		if (hands != null)
			msg += "!!" + hands.toString() + "%" + handsChance.toString() + "%";
		if (boots != null)
			msg += "!!" + boots.toString() + "%" + bootsChance.toString() + "%";
		
		return msg;
	}

	public boolean matches(CreatureEquipment equip) {
		if (equip == null) return false;
		
		if (head != null)
			if (head != equip.head) return false;
		if (chest != null)
			if (chest != equip.chest) return false;
		if (legs != null)
			if (legs != equip.legs) return false;
		if (hands != null)
			if (hands != equip.hands) return false;
		if (boots != null)
			if (boots != equip.boots) return false;
		
		return true;
	}
}
