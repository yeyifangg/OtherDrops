package com.gmail.zariust.otherdrops.drop;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import static com.gmail.zariust.common.CommonPlugin.enumValue;
import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.subject.Target;

public class CreatureDrop extends DropType {
	private CreatureType type;
	private Data data;
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
	
	public CreatureDrop(CreatureType mob, Data mobData) {
		this(1, mob, mobData);
	}
	
	public CreatureDrop(CreatureType mob, Data mobData, double percent) {
		this(1, mob, mobData, percent);
	}
	
	public CreatureDrop(int amount, CreatureType mob, Data mobData) {
		this(amount, mob, mobData, 100.0);
	}
	
	public CreatureDrop(int amount, CreatureType mob, Data mobData, double percent) { // Rome
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
	protected void performDrop(Target source, Location where, DropFlags flags) {
		int amount = quantity;
		while(amount-- > 0) {
			World in = where.getWorld();
			LivingEntity mob = in.spawnCreature(where, type);
			data.setOn(mob, flags.recipient);
		}
	}
	
	public static DropType parse(String drop, String state, int amount, double chance) {
		drop = drop.toUpperCase().replace("CREATURE_", "");
		String[] split = drop.split("@");
		if(split.length > 1) state = split[1];
		String name = split[0];
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = enumValue(CreatureType.class, name);
		// Log the name being parsed rather than creature.toString() to avoid NullPointerException
		OtherDrops.logInfo("Parsing the creature drop... creature="+name,EXTREME);
		if(creature == null) {
			CreatureGroup group = CreatureGroup.get(name);
			if(group == null) return null;
			return new ExclusiveDropGroup(group.creatures(), amount, chance);
		}
		Data data = CreatureData.parse(creature, state);
		OtherDrops.logInfo("Parsing the creature drop... creature="+creature.toString()+" data="+data.toString(),EXTREME);
		return new CreatureDrop(amount, creature, data, chance);
	}

	@Override
	public String toString() {
		String ret = "CREATURE_" + type;
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(type);
		return ret;
	}

	@Override
	public double getAmount() {
		return quantity;
	}
}
