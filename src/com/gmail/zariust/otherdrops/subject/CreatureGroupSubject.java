package com.gmail.zariust.otherdrops.subject;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.otherdrops.drops.AbstractDrop;

public class CreatureGroupSubject implements LivingSubject {
	private CreatureGroup group;
	
	public CreatureGroupSubject(CreatureGroup creature) {
		group = creature;
	}
	
	@Override
	public boolean overrideOn100Percent() {
		return true;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CreatureGroupSubject)) return false;
		return group == ((CreatureGroupSubject) other).group;
	}
	
	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemCategory.CREATURE, 0, group.hashCode());
	}
	
	@Override
	public boolean matches(Subject block) {
		if(!(block instanceof CreatureSubject)) return false;
		return group.contains(((CreatureSubject) block).getCreature());
	}
	
	@Override
	public List<Target> canMatch() {
		List<Target> all = new ArrayList<Target>();
		List<CreatureType> creatures = group.creatures();
		for(CreatureType type : creatures) all.add(new CreatureSubject(type));
		return all;
	}
	
	@Override
	public ItemCategory getType() {
		return ItemCategory.CREATURE;
	}
	
	@Override
	public void damage(int amount) {}
	
	@Override
	public void damageTool(short amount) {}
	
	@Override
	public void damageTool() {}
	
	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getKey() {
		return null;
	}

	public static CreatureGroupSubject parse(String name, @SuppressWarnings("unused") String state) {
		name = name.toUpperCase();
		if(!name.startsWith("CREATURE_")) name = "CREATURE_" + name;
		CreatureGroup creature = CreatureGroup.get(name);
		if(creature == null) return null;
		return new CreatureGroupSubject(creature);
	}

	@Override
	public String toString() {
		if(group == null) return "ANY_CREATURE";
		return group.toString();
	}
}
