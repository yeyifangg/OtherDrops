package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.data.CreatureData;
import com.gmail.zariust.bukkit.otherblocks.data.Data;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;

public class CreatureSubject implements LivingSubject {
	private CreatureType creature;
	private Data data;
	private LivingEntity agent;
	
	public CreatureSubject() {
		this((CreatureType) null);
	}
	
	public CreatureSubject(CreatureType tool) {
		this(tool, null);
	}
	
	public CreatureSubject(CreatureType tool, int d) {
		this(tool, new CreatureData(d));
	}
	
	public CreatureSubject(CreatureType tool, Data d) {
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
		return creature == tool.creature && data.getData() == tool.data.getData(); // must be data.getData() otherwise comparing different objects will always fail
	}

	@Override
	public boolean equals(Object other) {
		CreatureSubject tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Subject other) {
		if(other instanceof ProjectileAgent) return matches(((ProjectileAgent) other).getShooter());
		CreatureSubject tool = equalsHelper(other);
		if(creature == null) {
			OtherBlocks.logInfo("CreatureSubject.match - creature = null.", 5);
			return true;
		}
		if(data == null) {
			boolean match = (creature == tool.creature);
			OtherBlocks.logInfo("CreatureSubject.match - data = null. creature: "+creature.toString()+", tool.creature: "+tool.creature.toString()+", match="+match, 5);
			return match;
		}
		
		boolean match = isEqual(tool);
		OtherBlocks.logInfo("CreatureSubject.match - tool.creature="+tool.creature.toString()+", creature="+creature.toString()+", tooldata="+tool.data.toString()+", data="+data.toString()+", match=" + match, 5);
		return match;
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemCategory.CREATURE, creature == null ? 0 : creature.hashCode(), data.getData());
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getCreatureData() {
		return data.getData();
	}
	
	public LivingEntity getAgent() {
		return agent;
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.CREATURE;
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}

	public static LivingSubject parse(String name, String state) {
		// TODO: Is there a way to detect non-vanilla creatures?
		name = name.toUpperCase().replace("CREATURE_", "");
		CreatureType creature = CreatureType.valueOf(name);
		if(creature == null) {
			return CreatureGroupSubject.parse(name, state);
		}
		Data data = CreatureData.parse(creature, state);
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

	@Override
	public String toString() {
		if(creature == null) return "ANY_CREATURE";
		String ret = "CREATURE_" + creature.toString();
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(creature);
		return ret;
	}
}
