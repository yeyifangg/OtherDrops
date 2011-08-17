package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class CreatureTarget extends Target {
	private CreatureType creature;
	private int data;
	
	public CreatureTarget(LivingEntity entity) {
		this(CommonEntity.getCreatureType(entity), CommonEntity.getCreatureData(entity));
	}
	
	public CreatureTarget(CreatureType type, int d) {
		super(TargetType.CREATURE);
		creature = type;
		data = d;
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getData() {
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
}
