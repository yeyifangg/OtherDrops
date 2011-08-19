package com.gmail.zariust.bukkit.otherblocks.options.tool;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

@ConfigOnly(Agent.class)
public class AnyAgent implements Agent {
	@Override
	public boolean equals(Object other) {
		return other instanceof AnyAgent;
	}
	@Override
	public boolean matches(Agent other) {
		return true;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return AbstractDrop.hashCode(null, -42, 7);
	}
	@Override
	public ItemType getType() {
		return null;
	}
	@Override public void damage(int amount) {}
	@Override public void damageTool(short amount) {}
	@Override public void damageTool() {}
}