package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class CreatureAgent extends Agent {
	private CreatureType creature;
	private Integer data;
	private LivingEntity agent;
	
	public CreatureAgent() {
		this((CreatureType) null);
	}
	
	public CreatureAgent(CreatureType tool) {
		this(tool, null);
	}
	
	public CreatureAgent(CreatureType tool, Integer d) {
		super(ToolType.CREATURE);
		creature = tool;
		data = d;
	}
	
	public CreatureAgent(LivingEntity damager) {
		this(CommonEntity.getCreatureType(damager), CommonEntity.getCreatureData(damager));
		agent = damager;
	}
	
	private CreatureAgent equalsHelper(Object other) {
		if(!(other instanceof CreatureAgent)) return null;
		return (CreatureAgent) other;
	}

	private boolean isEqual(CreatureAgent tool) {
		if(tool == null) return false;
		return creature == tool.creature && data == tool.data;
	}

	@Override
	public boolean equals(Object other) {
		CreatureAgent tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		if(other instanceof ProjectileAgent) return matches(((ProjectileAgent) other).getShooter());
		CreatureAgent tool = equalsHelper(other);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}
	
	@Override
	protected int getIdHash() {
		return creature == null ? 0 : creature.hashCode();
	}
	
	@Override
	protected int getDataHash() {
		return data;
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getCreatureData() {
		return data;
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}
}
