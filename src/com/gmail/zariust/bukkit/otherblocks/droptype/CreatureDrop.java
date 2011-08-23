package com.gmail.zariust.bukkit.otherblocks.droptype;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.data.CreatureData;

public class CreatureDrop extends DropType {
	private CreatureType type;
	private CreatureData data;
	private int quantity;
	
	public CreatureDrop(CreatureType mob) {
		this(1, mob, 0);
	}
	
	public CreatureDrop(CreatureType mob, double percent) {
		this(1, mob, 0, percent);
	}
	
	public CreatureDrop(int amount, CreatureType mob) {
		this(amount, mob, 0);
	}
	
	public CreatureDrop(int amount, CreatureType mob, double percent) {
		this(amount, mob, 0, percent);
	}
	
	public CreatureDrop(CreatureType mob, int mobData) {
		this(1, mob, mobData);
	}
	
	public CreatureDrop(CreatureType mob, int mobData, double percent) {
		this(1, mob, mobData, percent);
	}
	
	public CreatureDrop(int amount, CreatureType mob, int mobData) {
		this(amount, mob, mobData, 100.0);
	}
	
	public CreatureDrop(int amount, CreatureType mob, int mobData, double percent) {
		this(amount, mob, new CreatureData(mobData), percent);
	}
	
	public CreatureDrop(CreatureType mob, CreatureData mobData) {
		this(1, mob, mobData);
	}
	
	public CreatureDrop(CreatureType mob, CreatureData mobData, double percent) {
		this(1, mob, mobData, percent);
	}
	
	public CreatureDrop(int amount, CreatureType mob, CreatureData mobData) {
		this(amount, mob, mobData, 100.0);
	}
	
	public CreatureDrop(int amount, CreatureType mob, CreatureData mobData, double percent) { // Rome
		super(DropCategory.CREATURE, percent);
		type = mob;
		data = mobData;
		quantity = amount;
	}
	
	public CreatureDrop(Entity e) {
		this(CommonEntity.getCreatureType(e), CommonEntity.getCreatureData(e));
	}

	public CreatureType getCreature() {
		return type;
	}

	public int getCreatureData() {
		return data.getData();
	}
	
	public int getQuantity() {
		return quantity;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		int amount = quantity;
		while(amount-- > 0) {
			World in = where.getWorld();
			LivingEntity mob = in.spawnCreature(where.add(0.5, 1, 0.5), type);
			data.setOn(mob, flags.recipient);
		}
	}
	
	public static DropType parse(String drop, String state, int amount, double chance) {
		String[] split = drop.split("@");
		if(split.length > 1) state = split[1];
		String name = split[0].replace("CREATURE_", "");
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = CreatureType.valueOf(name);
		if(creature == null) {
			CreatureGroup group = CreatureGroup.get(name);
			if(group == null) return null;
			return new ExclusiveDropGroup(group.creatures(), amount, chance);
		}
		//Integer intData = CommonEntity.parseCreatureData(creature, data);
		CreatureData data = CreatureData.parse(creature, state);
		return new CreatureDrop(amount, creature, data, chance);
	}
}
