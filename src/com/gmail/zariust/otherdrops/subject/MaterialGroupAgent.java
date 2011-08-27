package com.gmail.zariust.otherdrops.subject;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.options.ConfigOnly;

@ConfigOnly(PlayerSubject.class)
public class MaterialGroupAgent implements Agent {
	private MaterialGroup group;
	
	public MaterialGroupAgent(MaterialGroup g) {
		group = g;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof MaterialGroupAgent)) return false;
		return group == ((MaterialGroupAgent) other).group;
	}
	
	@Override
	public boolean matches(Subject other) {
		if(!(other instanceof PlayerSubject)) return false;
		return group.contains(((PlayerSubject) other).getMaterial());
	}
	
	@Override
	public int hashCode() {
		return AbstractDropEvent.hashCode(ItemCategory.PLAYER, 0, group.hashCode());
	}
	
	public List<Material> getMaterials() {
		return group.materials();
	}
	
	@Override
	public ItemCategory getType() {
		return ItemCategory.PLAYER;
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(short amount) {}
	
	@Override public void damageTool() {}

	@Override
	public String toString() {
		if(group == null) return "ANY_OBJECT";
		return group.toString();
	}
}