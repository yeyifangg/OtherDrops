package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

public class CreatureSubject implements LivingSubject {
	private CreatureType creature;
	private Integer data;
	private LivingEntity agent;
	
	public CreatureSubject() {
		this((CreatureType) null);
	}
	
	public CreatureSubject(CreatureType tool) {
		this(tool, null);
	}
	
	public CreatureSubject(CreatureType tool, Integer d) {
		creature = tool;
		data = d;
	}
	
	public CreatureSubject(LivingEntity damager) {
		this(CommonEntity.getCreatureType(damager), CommonEntity.getCreatureData(damager));
		agent = damager;
	}
	
	private CreatureSubject equalsHelper(Object other) {
		if(!(other instanceof CreatureSubject)) return null;
		return (CreatureSubject) other;
	}

	private boolean isEqual(CreatureSubject tool) {
		if(tool == null) return false;
		return creature == tool.creature && data == tool.data;
	}

	@Override
	public boolean equals(Object other) {
		CreatureSubject tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		if(other instanceof ProjectileAgent) return matches((Agent) ((ProjectileAgent) other).getShooter());
		CreatureSubject tool = equalsHelper(other);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Target block) {
		CreatureSubject tool = equalsHelper(block);
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

	public static LivingSubject parse(String name, String state) {
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = CreatureType.fromName(name);
		if(creature == null) {
			return CreatureGroupSubject.parse(name, state);
		}
		Integer data = CommonEntity.parseCreatureData(creature, state);
		return new CreatureSubject(creature, data);
	}

	@Override
	public Location getLocation() {
		if(agent != null) return agent.getLocation();
		return null;
	}

	@Override
	public List<Target> canMatch() {
		if(creature == null) return new CreatureGroupSubject(CreatureGroup.CREATURE_ANY).canMatch();
		return Collections.singletonList((Target) this);
	}

	@Override
	public String getKey() {
		if(creature != null) return creature.toString();
		return null;
	}
}
