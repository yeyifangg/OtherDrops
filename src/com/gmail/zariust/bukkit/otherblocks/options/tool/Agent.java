package com.gmail.zariust.bukkit.otherblocks.options.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.ConfigurationNode;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocksConfig;

public abstract class Agent {
	public enum ToolType {PLAYER, CREATURE, PROJECTILE, DAMAGE};
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
		Integer intData;
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
		if(data.isEmpty()) intData = null;
		else try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {
			intData = (int) CommonMaterial.getAnyDataShort(name, data);
		}
		if(name.startsWith("ANY")) {
			if(name.endsWith("ANY")) return ANY;
			else if(name.equals("ANY_OBJECT")) return ANY_OBJECT;
			else if(name.equals("ANY_CREATURE")) return ANY_CREATURE;
			else if(name.equals("ANY_DAMAGE")) return ANY_DAMAGE;
			else if(name.equals("ANY_PROJECTILE")) return ANY_PROJECTILE;
			MaterialGroup group = MaterialGroup.get(name);
			if(group != null) return new MaterialGroupAgent(group);
			return null;
		} else if(name.startsWith("DAMAGE_")) {
			DamageCause cause;
			try {
				cause = DamageCause.valueOf(name.substring(7));
				if(cause == DamageCause.FIRE_TICK || cause == DamageCause.CUSTOM) return null;
			} catch(IllegalArgumentException e) {
				if(name.equals("DAMAGE_WATER")) cause = DamageCause.CUSTOM;
				else return null;
			}
			return new EnvironmentAgent(cause);
		} else if(name.startsWith("CREATURE_")) {
			CreatureType creature = CreatureType.fromName(name.substring(9));
			if(creature != null) return new CreatureAgent(creature, intData);
			else return null;
		} else if(name.startsWith("PROJECTILE_")) {
			name = name.substring(11);
			// Parse data, which is one of the following
			// - A CreatureType constant (note that only GHAST and SKELETON will actually do anything
			//   unless there's some other plugin making entities shoot things)
			// - One of the special words PLAYER or DISPENSER
			// - Something else, which is taken to be a player name
			CreatureType creature = CreatureType.fromName(data);
			if(name.equals("FIRE") || name.equals("FIREBALL"))
				return new ProjectileAgent(Material.FIRE);
			else if(name.equals("SNOW_BALL"))
				return new ProjectileAgent(Material.SNOW_BALL);
			else if(name.equals("EGG"))
				return new ProjectileAgent(Material.EGG);
			else if(name.equals("FISH") || name.equals("FISHING_ROD"))
				return new ProjectileAgent(Material.FISHING_ROD);
			else if(name.equals("ARROW"))
				return new ProjectileAgent(Material.ARROW);
		}
	}

	public static Map<Agent, Boolean> parseFrom(ConfigurationNode node) {
		List<String> tools = OtherBlocksConfig.getMaybeList(node, "tool");
		List<String> toolsExcept = OtherBlocksConfig.getMaybeList(node, "toolexcept");
		if(tools.isEmpty() && toolsExcept.isEmpty()) return null;
		Map<Agent, Boolean> toolMap = new HashMap<Agent, Boolean>();
		for(String tool : tools) {
			Agent agent = null;
			boolean flag = true;
			if(tool.startsWith("-")) {
				agent = parse(tool.substring(1));
				flag = false;
			} else agent = parse(tool);
			if(agent instanceof MaterialGroupAgent) {
				for(Material mat : ((MaterialGroupAgent) agent).getMaterials())
					toolMap.put(new PlayerAgent(mat), flag);
			} else toolMap.put(agent, flag);
		}
		for(String tool : toolsExcept) {
			Agent agent = parse(tool);
			if(agent instanceof MaterialGroupAgent) {
				for(Material mat : ((MaterialGroupAgent) agent).getMaterials())
					toolMap.put(new PlayerAgent(mat), false);
			} else toolMap.put(agent, false);
		}
		return toolMap;
	}
	
	private static class MaterialGroupAgent extends Agent {
		private MaterialGroup group;
		protected MaterialGroupAgent(MaterialGroup g) {
			super(ToolType.PLAYER);
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
		public List<Material> getMaterials() {
			return group.materials();
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
