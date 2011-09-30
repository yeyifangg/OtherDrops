package com.gmail.zariust.otherdrops.subject;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;

import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.ConfigOnly;
import com.gmail.zariust.otherdrops.options.ToolDamage;

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
		return new HashCode(this).get(group);
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
	
	@Override public void damageTool(ToolDamage amount, Random rng) {}

	@Override
	public String toString() {
		if(group == null) return "ANY_OBJECT";
		return group.toString();
	}

	@Override
	public Data getData() {
		return null;
	}
}