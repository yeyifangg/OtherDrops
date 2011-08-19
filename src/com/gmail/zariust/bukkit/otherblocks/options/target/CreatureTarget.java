package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.options.CreatureOption;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public class CreatureTarget implements Target, CreatureOption {
	private CreatureType creature;
	private int data;
	
	public CreatureTarget(LivingEntity entity) {
		this(CommonEntity.getCreatureType(entity), CommonEntity.getCreatureData(entity));
	}
	
	public CreatureTarget(CreatureType type, int d) {
		creature = type;
		data = d;
	}
	
	@Override
	public CreatureType getCreature() {
		return creature;
	}
	
	@Override
	public int getCreatureData() {
		return data;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CreatureTarget)) return false;
		CreatureTarget targ = (CreatureTarget) other;
		return creature == targ.creature && data == targ.data;
	}
	
	@Override
	public int hashCode() {
		return (data << 16) | creature.hashCode();
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override
	public ItemType getType() {
		return ItemType.CREATURE;
	}

	@Override
	public boolean matches(Target block) {
		return equals(block);
	}
}
