package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

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
		return AbstractDrop.hashCode(ItemType.CREATURE, 0, group.hashCode());
	}
	
	@Override
	public boolean matches(Target block) {
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
	public boolean matches(Agent other) {
		if(!(other instanceof CreatureSubject)) return false;
		return group.contains(((CreatureSubject) other).getCreature());
	}
	
	@Override
	public ItemType getType() {
		return ItemType.CREATURE;
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
		CreatureGroup creature = CreatureGroup.get(name);
		if(creature == null) return null;
		return new CreatureGroupSubject(creature);
	}
	
}
