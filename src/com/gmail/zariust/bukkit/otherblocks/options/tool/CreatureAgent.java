package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.DyeColor;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;

public class CreatureAgent implements LivingAgent, Target {
	private CreatureType creature;
	private Integer data;
	private LivingEntity agent;
	
	public CreatureAgent() {
		this((CreatureType) null);
	}
	
	public CreatureAgent(CreatureType tool) {
		this(tool, null);
	}
	
	public CreatureAgent(CreatureType tool, Integer d) {
		creature = tool;
		data = d;
	}
	
	public CreatureAgent(LivingEntity damager) {
		this(CommonEntity.getCreatureType(damager), CommonEntity.getCreatureData(damager));
		agent = damager;
	}
	
	private CreatureAgent equalsHelper(Object other) {
		if(!(other instanceof CreatureAgent)) return null;
		return (CreatureAgent) other;
	}

	private boolean isEqual(CreatureAgent tool) {
		if(tool == null) return false;
		return creature == tool.creature && data == tool.data;
	}

	@Override
	public boolean equals(Object other) {
		CreatureAgent tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		if(other instanceof ProjectileAgent) return matches(((ProjectileAgent) other).getShooter());
		CreatureAgent tool = equalsHelper(other);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Target block) {
		CreatureAgent tool = equalsHelper(block);
		if(creature == null) return true;
		if(data == null) return creature == tool.creature;
		return isEqual(tool);
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.CREATURE, creature == null ? 0 : creature.hashCode(), data);
	}
	
	public CreatureType getCreature() {
		return creature;
	}
	
	public int getCreatureData() {
		return data;
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}

	@Override
	public ItemType getType() {
		return ItemType.CREATURE;
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}

	@SuppressWarnings("incomplete-switch")
	public static CreatureAgent parse(String name, String state) {
		// TODO: Is there a way to detect non-vanilla creatures?
		CreatureType creature = CreatureType.fromName(name.substring(9));
		if(creature == null) {
			// TODO: Creature groups!
			return null;
		}
		switch(creature) {
		case CREEPER:
			if(state.equalsIgnoreCase("POWERED")) return new CreatureAgent(creature, 1);
			else if(state.equalsIgnoreCase("UNPOWERED")) return new CreatureAgent(creature, 0);
			break;
		case PIG:
			if(state.equalsIgnoreCase("SADDLED")) return new CreatureAgent(creature, 1);
			else if(state.equalsIgnoreCase("UNSADDLED")) return new CreatureAgent(creature, 0);
			break;
		case SHEEP:
			String[] split = state.split("/");
			if(split.length <= 2) {
				int data;
				String colour = "", wool = "";
				if(split[0].endsWith("SHEARED")) {
					wool = split[0];
					if(split.length == 2) colour = split[1];
				} else if(split.length == 2 && split[1].endsWith("SHEARED")) {
					wool = split[1];
					colour = split[0];
				} else colour = split[0];
				if(!colour.isEmpty() || !wool.isEmpty()) {
					try {
						data = DyeColor.valueOf(colour).getData();
						if(state.equalsIgnoreCase("SHEARED")) return new CreatureAgent(creature, data + 16);
						else if(state.equalsIgnoreCase("UNSHEARED")) return new CreatureAgent(creature, data);
					} catch(IllegalArgumentException e) {}
				}
			}
			break;
		case SLIME:
			if(state.equalsIgnoreCase("TINY")) return new CreatureAgent(creature, 1);
			else if(state.equalsIgnoreCase("SMALL")) return new CreatureAgent(creature, 2);
			else if(state.equalsIgnoreCase("BIG")) return new CreatureAgent(creature, 3);
			else if(state.equalsIgnoreCase("HUGE")) return new CreatureAgent(creature, 4);
			// Fallthrough intentional
		case PIG_ZOMBIE:
			try {
				int sz = Integer.parseInt(state);
				return new CreatureAgent(creature, sz);
			} catch(NumberFormatException e) {}
			break;
		case WOLF:
			if(state.equalsIgnoreCase("TAME") || state.equalsIgnoreCase("TAMED"))
				return new CreatureAgent(creature, 2);
			else if(state.equalsIgnoreCase("WILD") || state.equalsIgnoreCase("NEUTRAL"))
				return new CreatureAgent(creature, 0);
			else if(state.equalsIgnoreCase("ANGRY")) return new CreatureAgent(creature, 1);
			break;
		}
		return new CreatureAgent(creature, null);
	}
}
