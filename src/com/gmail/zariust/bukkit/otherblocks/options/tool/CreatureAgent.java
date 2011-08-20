package com.gmail.zariust.bukkit.otherblocks.options.tool;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;

public class CreatureAgent implements LivingAgent {
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
		if(other instanceof ProjectileAgent) return matches((Agent) ((ProjectileAgent) other).getShooter());
		CreatureAgent tool = equalsHelper(other);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Target block) {
		CreatureAgent tool = equalsHelper(block);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.CREATURE, creature == null ? 0 : creature.hashCode(), data);
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getCreatureData() {
		return data;
	}
	
	public LivingEntity getAgent() {
		return agent;
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}

	@Override
	public ItemType getType() {
		return ItemType.CREATURE;
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}

	public static LivingAgent parse(String name, String state) {
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = CreatureType.fromName(name);
		if(creature == null) {
			return CreatureGroupAgent.parse(name, state);
		}
		Integer data = CommonEntity.parseCreatureData(creature, state);
		return new CreatureAgent(creature, data);
	}

	@Override
	public Location getLocation() {
		if(agent != null) return agent.getLocation();
		return null;
	}

	@Override
	public List<Target> canMatch() {
		if(creature == null) return new CreatureGroupAgent(CreatureGroup.CREATURE_ANY).canMatch();
		return Collections.singletonList((Target) this);
	}

	@Override
	public String getKey() {
		if(creature != null) return creature.toString();
		return null;
	}
}
