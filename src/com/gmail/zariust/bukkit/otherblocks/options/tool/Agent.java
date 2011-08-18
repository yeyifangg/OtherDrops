package com.gmail.zariust.bukkit.otherblocks.options.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;

public abstract class Agent {
	public enum ToolType {ITEM, CREATURE, PROJECTILE, DAMAGE};
	public final static Agent ANY = new AnyAgent();
	public final static Agent ANY_OBJECT = new PlayerAgent();
	public final static Agent ANY_CREATURE = new CreatureAgent();
	public final static Agent ANY_DAMAGE = new EnvironmentAgent();
	public final static Agent ANY_PROJECTILE = new ProjectileAgent();
	public final static Agent FLOW = new EnvironmentAgent(DamageCause.CUSTOM);

	private ToolType type;
	
	protected Agent(ToolType t) {
		type = t;
	}

	@Override
	public abstract boolean equals(Object other);

	public abstract boolean matches(Agent other);

	@Override
	public int hashCode() {
		short t = (short) type.hashCode();
		int v = getIdHash(), data = getDataHash();
		return (v << 16) | t | (data << 3);
	}

	protected abstract int getDataHash();

	protected abstract int getIdHash();

	public ToolType getType() {
		return type;
	}
	
	public void damage(int amount) {}

	public void damageTool(short amount) {}
	
	public void damageTool() {}
	
	public static Agent parse(String agent) {
		String[] split = agent.split("@");
		String name = split[0].toUpperCase(), data = "";
		int intData;
		if(split.length > 1) data = split[1];
		// Agent can be one of the following
		// - A tool; ie, a Material constant
		// - One of the Material synonyms NOTHING and DYE
		// - A MaterialGroup constant
		// - One of the special wildcards ANY, ANY_CREATURE, ANY_DAMAGE
		// - A DamageCause constant prefixed by DAMAGE_
		//   - DAMAGE_FIRE_TICK and DAMAGE_CUSTOM are valid but not allowed
		//   - DAMAGE_WATER is invalid but allowed, and stored as CUSTOM
		// - A CreatureType constant prefixed by CREATURE_
		// - A projectile; ie a Material constant prefixed by PROJECTILE_
	}

	public static Map<Agent, Boolean> parseFrom(ConfigurationNode node) {
		List<String> tools = OtherBlocksConfig.getMaybeList(node, "tool");
		List<String> toolsExcept = OtherBlocksConfig.getMaybeList(node, "toolexcept");
		if(tools.isEmpty() && toolsExcept.isEmpty()) return null;
		Map<Agent, Boolean> toolMap = new HashMap<Agent, Boolean>();
		
	}
	
	private static class MaterialGroupAgent extends Agent {
		private MaterialGroup group;
		protected MaterialGroupAgent(MaterialGroup g) {
			super(ToolType.ITEM);
			group = g;
		}
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof MaterialGroupAgent)) return false;
			return group == ((MaterialGroupAgent) other).group;
		}
		@Override
		public boolean matches(Agent other) {
			if(!(other instanceof PlayerAgent)) return false;
			return group.contains(((PlayerAgent) other).getMaterial());
		}
		@Override
		protected int getDataHash() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		protected int getIdHash() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	private static class AnyAgent extends Agent {
		private AnyAgent() {
			super(null);
		}
		@Override
		public boolean equals(Object other) {
			return other == ANY;
		}
		@Override
		public boolean matches(Agent other) {
			return true;
		}
		@Override
		protected int getDataHash() {
			return 7;
		}
		@Override
		protected int getIdHash() {
			return -42;
		}
	}
}
