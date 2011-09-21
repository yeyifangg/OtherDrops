package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import static com.gmail.zariust.common.CommonPlugin.enumValue;
import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.options.ToolDamage;

public class CreatureSubject extends LivingSubject {
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
		this(tool, d, null);
	}
	
	public CreatureSubject(LivingEntity damager) {
		this(CommonEntity.getCreatureType(damager), CommonEntity.getCreatureData(damager), damager);
		agent = damager;
	}
	
	public CreatureSubject(CreatureType tool, int d, LivingEntity damager) {
		this(tool, new CreatureData(d), damager);
	}
	
	public CreatureSubject(CreatureType tool, Data d, LivingEntity damager) {
		super(damager);
		creature = tool;
		data = d;
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
		if(tool == null) return false;
		if(creature == null) {
			OtherDrops.logInfo("CreatureSubject.match - creature = null.", EXTREME);
			return true;
		}
		if(data == null) {
			boolean match = (creature == tool.creature);
			OtherDrops.logInfo("CreatureSubject.match - data = null. creature: "+creature.toString()+", tool.creature: "+tool.creature.toString()+", match="+match, EXTREME);
			return match;
		}
		
		boolean match = isEqual(tool);
		OtherDrops.logInfo("CreatureSubject.match - tool.creature="+tool.creature.toString()+", creature="+creature.toString()+", tooldata="+tool.data.toString()+", data="+String.valueOf(data)+", match=" + match, EXTREME);
		return match;
	}

	@Override
	public int hashCode() {
		return AbstractDropEvent.hashCode(ItemCategory.CREATURE, creature == null ? 0 : creature.hashCode(), data == null ? 0 : data.getData());
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

	@Override public void damageTool(ToolDamage amount) {}

	public static LivingSubject parse(String name, String state) {
		// TODO: Is there a way to detect non-vanilla creatures?
		name = name.toUpperCase().replace("CREATURE_", "");
		CreatureType creature = enumValue(CreatureType.class, name);
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
