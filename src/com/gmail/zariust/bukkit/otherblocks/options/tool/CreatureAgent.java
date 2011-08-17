package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class CreatureAgent extends Agent {
	private CreatureType creature;
	private int data;
	private LivingEntity agent;
	
	public CreatureAgent(CreatureType tool) {
		super(ToolType.CREATURE);
		creature = tool;
	}
	
	public CreatureAgent(CreatureType tool, int d) {
		this(tool);
		data = d;
	}
	
	public CreatureAgent(LivingEntity damager) {
		this(CommonEntity.getCreatureType(damager), CommonEntity.getCreatureData(damager));
		agent = damager;
	}

	@Override
	protected boolean matches(Agent other) {
		if(other instanceof CreatureAgent) return matches((CreatureAgent) other);
		return false;
	}
	
	private boolean matches(CreatureAgent other) {
		if(creature == null || other.creature == null) return true;
		return creature == other.creature && data == other.data;
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
