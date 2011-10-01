// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;

import static com.gmail.zariust.common.CommonPlugin.enumValue;
import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class CreatureDrop extends DropType {
	private CreatureType type;
	private Data data;
	private IntRange quantity;
	private int rolledQuantity;
	
	public CreatureDrop(CreatureType mob) {
		this(new IntRange(1), mob, 0);
	}
	
	public CreatureDrop(CreatureType mob, double percent) {
		this(new IntRange(1), mob, 0, percent);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob) {
		this(amount, mob, 0);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob, double percent) {
		this(amount, mob, 0, percent);
	}
	
	public CreatureDrop(CreatureType mob, int mobData) {
		this(new IntRange(1), mob, mobData);
	}
	
	public CreatureDrop(CreatureType mob, int mobData, double percent) {
		this(new IntRange(1), mob, mobData, percent);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob, int mobData) {
		this(amount, mob, mobData, 100.0);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob, int mobData, double percent) {
		this(amount, mob, new CreatureData(mobData), percent);
	}
	
	public CreatureDrop(CreatureType mob, Data mobData) {
		this(new IntRange(1), mob, mobData);
	}
	
	public CreatureDrop(CreatureType mob, Data mobData, double percent) {
		this(new IntRange(1), mob, mobData, percent);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob, Data mobData) {
		this(amount, mob, mobData, 100.0);
	}
	
	public CreatureDrop(IntRange amount, CreatureType mob, Data mobData, double percent) { // Rome
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

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		rolledQuantity = quantity.getRandomIn(flags.rng);
		int amount = rolledQuantity;
		while(amount-- > 0) drop(where, flags.recipient, type, data);
	}
	
	public static DropType parse(String drop, String state, IntRange amount, double chance) {
		drop = drop.toUpperCase().replace("CREATURE_", "");
		String[] split = drop.split("@");
		if(split.length > 1) state = split[1];
		String name = split[0];
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = enumValue(CreatureType.class, name);
		// Log the name being parsed rather than creature.toString() to avoid NullPointerException
		OtherDrops.logInfo("Parsing the creature drop... creature="+name,EXTREME);
		if(creature == null) {
			if(name.startsWith("^")) {
				name = name.substring(1);
				CreatureGroup group = CreatureGroup.get(name);
				if(group == null) return null;
				return new SimpleDropGroup(group.creatures(), amount, chance);
			} else {
				CreatureGroup group = CreatureGroup.get(name);
				if(group == null) return null;
				return new ExclusiveDropGroup(group.creatures(), amount, chance);
			}
		}
		Data data = CreatureData.parse(creature, state);
		OtherDrops.logInfo("Parsing the creature drop... creature="+creature.toString()+" data="+data.get(creature),EXTREME);
		return new CreatureDrop(amount, creature, data, chance);
	}

	@Override
	public String getName() {
		String ret = "CREATURE_" + type;
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(type);
		return ret;
	}

	@Override
	public double getAmount() {
		return rolledQuantity;
	}

	@Override
	public DoubleRange getAmountRange() {
		return quantity.toDoubleRange();
	}
}
