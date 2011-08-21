package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

@ConfigOnly(Agent.class)
public class AnyAgent implements Agent, Target {
	@Override
	public boolean equals(Object other) {
		return other instanceof AnyAgent;
	}
	
	@Override
	public boolean matches(Agent other) {
		return true;
	}

	@Override
	public boolean matches(Target block) {
		return true;
	}
	
	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(null, -42, 7);
	}
	
	@Override
	public ItemType getType() {
		return null;
	}
	
	public static Agent parseAgent(String name) {
		if(name.endsWith("ANY")) return new AnyAgent();
		else if(name.equals("ANY_OBJECT")) return new PlayerAgent();
		else if(name.equals("ANY_CREATURE")) return new CreatureAgent();
		else if(name.equals("ANY_DAMAGE")) return new EnvironmentAgent();
		else if(name.equals("ANY_PROJECTILE")) return new ProjectileAgent();
		MaterialGroup group = MaterialGroup.get(name);
		if(group != null) return new MaterialGroupAgent(group);
		return null;
	}

	public static Target parseTarget(String name) {
		if(name.endsWith("ANY")) return new AnyAgent();
		else if(name.equals("ANY_BLOCK")) return new BlockTarget();
		else if(name.equals("ANY_CREATURE")) return new CreatureAgent();
		MaterialGroup group = MaterialGroup.get(name);
		if(group != null && group.isBlock()) return new BlocksTarget(group);
		else return null;
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(short amount) {}
	
	@Override public void damageTool() {}

	@Override
	public List<Target> canMatch() {
		List<Target> all = new ArrayList<Target>();
		all.addAll(new BlockTarget().canMatch());
		all.addAll(new CreatureAgent().canMatch());
		return all;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public Location getLocation() {
		return null;
	}
}