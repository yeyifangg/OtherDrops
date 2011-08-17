package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class CreatureTarget extends Target {
	private CreatureType creature;
	private int data;
	
	public CreatureTarget(LivingEntity entity) {
		super(TargetType.CREATURE);
		creature = CommonEntity.getCreatureType(entity);
		data = CommonEntity.getCreatureData(entity);
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getData() {
		return data;
	}
}
