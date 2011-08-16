package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.material.MaterialData;

public class Agent {
	public enum ToolType {ITEM, CREATURE, PROJECTILE, DAMAGE, SPECIAL};
	public final static Agent ANY = new Agent((ToolType) null);
	public final static Agent ANY_ITEM = new Agent((Material) null);
	public final static Agent ANY_CREATURE = new Agent((CreatureType) null);
	public final static Agent ANY_DAMAGE = new Agent((DamageCause) null);
	public final static Agent ANY_PROJECTILE = new Agent((Material) null, (CreatureType) null);
	public final static Agent LEAF_DECAY = new Agent(ToolType.SPECIAL);
	public final static Agent FLOW = new Agent(ToolType.SPECIAL);
	
	private ToolType type;
	private Material mat;
	private int data;
	private CreatureType creature;
	private DamageCause dmg;
	
	private Agent(ToolType t) {
		type = t;
	}
	
	public Agent(Material tool) {
		this(ToolType.ITEM);
		mat = tool;
	}
	
	public Agent(MaterialData tool) {
		this(tool.getItemType());
		data = tool.getData();
	}
	
	public Agent(CreatureType tool) {
		this(ToolType.CREATURE);
		creature = tool;
	}
	
	public Agent(CreatureType tool, int d) {
		this(tool);
		data = d;
	}
	
	public Agent(DamageCause tool) {
		this(ToolType.DAMAGE);
		dmg = tool;
	}
	
	public Agent(Material missile, CreatureType shooter) {
		this(ToolType.PROJECTILE);
		mat = missile;
		creature = shooter;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Agent)) return false;
		return equals((Agent) other);
	}
	
	public boolean equals(Agent other) {
		if(type == null || other.type == null) return true;
		if(type != other.type) return false;
		switch(type) {
		case CREATURE:
			if(creature == null || other.creature == null) return true;
			return creature == other.creature && data == other.data;
		case DAMAGE:
			if(dmg == null || other.dmg == null) return true;
			return dmg == other.dmg;
		case ITEM:
			if(mat == null || other.mat == null) return true;
			return mat == other.mat && data == other.data;
		case PROJECTILE:
			if(creature == null || other.creature == null || mat == null || other.mat == null) return true;
			return creature == other.creature && data == other.data && mat == other.mat && data == other.data;
		case SPECIAL:
			// Yes, for SPECIAL the .equals does reference comparison
			return this == other;
		}
		return false;
	}

	@Override
	public int hashCode() {
		short t = (short) type.hashCode();
		int v = 0;
		switch(type) {
		case CREATURE:
			v = creature == null ? 0 : creature.hashCode();
			break;
		case DAMAGE:
			v = dmg == null ? 0 : dmg.hashCode();
			break;
		case ITEM:
			v = mat == null ? 0 : mat.hashCode();
			break;
		case PROJECTILE:
			v = (creature == null ? 0 : creature.hashCode()) ^ (mat == null ? 0 : mat.hashCode());
			break;
		case SPECIAL:
			v = -42;
			break;
		}
		return (v << 16) | t | (data << 3);
	}

	public ToolType getType() {
		return type;
	}
}
