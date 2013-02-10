package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.drop.ItemDrop;

public class SkeletonData extends CreatureData {
	SkeletonType type = null; // null = wildcard
	Integer maxHealth = null;
	CreatureEquipment equip = null;
	
	public SkeletonData(SkeletonType type, Integer maxHealth, CreatureEquipment equip) {
		this.type = type;
		this.maxHealth = maxHealth;
		this.equip = equip;
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		if (mob instanceof Skeleton) {
			Skeleton z = (Skeleton)mob;
			if (type != null) z.setSkeletonType(type);
			if (maxHealth != null) {
				z.setMaxHealth(maxHealth);
				z.setHealth(maxHealth);
			}
			if (equip != null) {
				if (equip.head != null) z.getEquipment().setHelmet(equip.head);
				if (equip.headChance != null) z.getEquipment().setHelmetDropChance(equip.headChance);
				if (equip.hands != null) z.getEquipment().setItemInHand(equip.hands);
				if (equip.handsChance != null) z.getEquipment().setItemInHandDropChance(equip.handsChance);
				if (equip.chest != null) z.getEquipment().setChestplate(equip.chest);
				if (equip.chestChance != null) z.getEquipment().setChestplateDropChance(equip.chestChance);
				if (equip.legs != null) z.getEquipment().setLeggings(equip.legs);
				if (equip.legsChance != null) z.getEquipment().setLeggingsDropChance(equip.legsChance);
				if (equip.boots != null) z.getEquipment().setBoots(equip.boots);
				if (equip.bootsChance != null) z.getEquipment().setBootsDropChance(equip.bootsChance);

			}
		}
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof SkeletonData)) return false;
		SkeletonData vd = (SkeletonData)d;

		if (this.type != null)
			if (this.type != vd.type) return false;
		if (this.maxHealth != null)
			if (this.maxHealth != vd.maxHealth) return false; 
		
		return true;
	}

	public static CreatureData parseFromEntity(Entity entity) {
		if (entity instanceof Skeleton) {
			return new SkeletonData(((Skeleton)entity).getSkeletonType(), ((Skeleton)entity).getMaxHealth(), null);
		} else {
			Log.logInfo("SkeletonData: error, parseFromEntity given different creature - this shouldn't happen.");
			return null;
		}
		
	}
	
	public static CreatureData parseFromString(String state) {
		SkeletonType type = null;
		Integer maxHealth = null;
		CreatureEquipment equip = null;

		if (!state.isEmpty() && !state.equals("0")) {
			String split[] = state.split(OtherDropsConfig.CreatureDataSeparator);

			for (String sub : split) {

				if (sub.matches("[0-9]+")) { // need to check numbers before any .toLowerCase()
					maxHealth = Integer.valueOf(sub);
				} else {
					sub = sub.toLowerCase().replaceAll("[\\s-_]",  "");
					if (sub.equalsIgnoreCase("wither"))   type = SkeletonType.WITHER;
					else if (sub.equalsIgnoreCase("normal")) type = SkeletonType.NORMAL;
					else if (sub.startsWith("eq:")) {
						if (equip == null) equip = new CreatureEquipment();
						equip = parseEquipmentString(sub, equip);
					}
				}
			}
		}

		return new SkeletonData(type, maxHealth, equip);
	}
	
	private static CreatureEquipment parseEquipmentString(String sub, CreatureEquipment passEquip) {
		CreatureEquipment equip = passEquip;
		String subSplit[] = sub.split(":");

		if (subSplit.length == 3) {
			String split[] = subSplit[2].split("<");
			String slot = split[0];
			float chance = 0;
			if (split.length > 1) {
				chance = Float.valueOf(split[1]) /100; 
			}
			slot = slot.replace("\\!", "!");
			
			if (subSplit[1].matches("head")) {
				equip.head = ((ItemDrop)ItemDrop.parse(slot, "")).getItem();
				equip.headChance = chance;
			} else if (subSplit[1].matches("(hands|holding)")) {
				equip.hands = ((ItemDrop)ItemDrop.parse(slot, "")).getItem();
				equip.handsChance = chance;
			} else if (subSplit[1].matches("(chest|chestplate)")) {
				equip.chest = ((ItemDrop)ItemDrop.parse(slot, "")).getItem();
				equip.chestChance = chance;
			} else if (subSplit[1].matches("(legs|leggings)")) {
				equip.legs = ((ItemDrop)ItemDrop.parse(slot, "")).getItem();
				equip.legsChance = chance;
			} else if (subSplit[1].matches("(feet|boots)")) {
				equip.boots = ((ItemDrop)ItemDrop.parse(slot, "")).getItem();
				equip.bootsChance = chance;
			}
		}
		return equip;
		
	}

	public String toString() {
		String val = "";
		if (type != null) {
			val += "!";
			val += type.name();
		}
		return val;
	}
	
	@Override
	public String get(Enum<?> creature) {
		if(creature instanceof EntityType) return this.toString();
		return "";
	}
	
}

class CreatureEquipment {
	public ItemStack head;
	public Float headChance;
	public ItemStack chest;
	public Float chestChance;
	public ItemStack legs;
	public Float legsChance;
	public ItemStack hands;
	public Float handsChance;
	public ItemStack boots;
	public Float bootsChance;
	
}
