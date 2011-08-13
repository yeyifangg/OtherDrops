package com.gmail.zariust.bukkit.otherblocks.options;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.material.MaterialData;

public class Tool {
	public enum ToolType {ITEM, CREATURE, DAMAGE, SPECIAL};
	public final static Tool ANY = new Tool((ToolType) null);
	public final static Tool ANY_ITEM = new Tool((Material) null);
	public final static Tool ANY_CREATURE = new Tool((CreatureType) null);
	public final static Tool ANY_DAMAGE = new Tool((DamageCause) null);
	public final static Tool LEAF_DECAY = new Tool(ToolType.SPECIAL);
	
	private ToolType type;
	private Material mat;
	private int data;
	private CreatureType creature;
	private DamageCause dmg;
	
	private Tool(ToolType t) {
		type = t;
	}
	
	public Tool(Material tool) {
		this(ToolType.ITEM);
		mat = tool;
	}
	
	public Tool(MaterialData tool) {
		this(tool.getItemType());
		data = tool.getData();
	}
	
	public Tool(CreatureType tool) {
		this(ToolType.CREATURE);
		creature = tool;
	}
	
	public Tool(CreatureType tool, int d) {
		this(tool);
		data = d;
	}
	
	public Tool(DamageCause tool) {
		this(ToolType.DAMAGE);
		dmg = tool;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tool)) return false;
		return equals((Tool) other);
	}
	
	public boolean equals(Tool other) {
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
		case SPECIAL:
			return true;
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
		case SPECIAL:
			v = -42;
			break;
		}
		return (v << 16) | t | (data << 3);
	}
}
