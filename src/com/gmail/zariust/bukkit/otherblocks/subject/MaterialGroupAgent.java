package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

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
		return AbstractDrop.hashCode(ItemType.PLAYER, 0, group.hashCode());
	}
	
	public List<Material> getMaterials() {
		return group.materials();
	}
	
	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(short amount) {}
	
	@Override public void damageTool() {}
}