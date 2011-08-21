package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.bukkit.common.CreatureGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

public class CreatureGroupAgent implements LivingAgent {
	private CreatureGroup group;
	
	public CreatureGroupAgent(CreatureGroup creature) {
		group = creature;
	}
	
	@Override
	public boolean overrideOn100Percent() {
		return true;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CreatureGroupAgent)) return false;
		return group == ((CreatureGroupAgent) other).group;
	}
	
	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.CREATURE, 0, group.hashCode());
	}
	
	@Override
	public boolean matches(Target block) {
		if(!(block instanceof CreatureAgent)) return false;
		return group.contains(((CreatureAgent) block).getCreature());
	}
	
	@Override
	public List<Target> canMatch() {
		List<Target> all = new ArrayList<Target>();
		List<CreatureType> creatures = group.creatures();
		for(CreatureType type : creatures) all.add(new CreatureAgent(type));
		return all;
	}
	
	@Override
	public boolean matches(Agent other) {
		if(!(other instanceof CreatureAgent)) return false;
		return group.contains(((CreatureAgent) other).getCreature());
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

	public static CreatureGroupAgent parse(String name, @SuppressWarnings("unused") String state) {
		CreatureGroup creature = CreatureGroup.get(name);
		if(creature == null) return null;
		return new CreatureGroupAgent(creature);
	}
	
}
