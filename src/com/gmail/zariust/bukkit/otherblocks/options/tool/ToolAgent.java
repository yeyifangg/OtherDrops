package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.MaterialOption;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

@ConfigOnly(PlayerAgent.class)
public class ToolAgent implements Agent, MaterialOption {
	private Integer id, data;
	
	public ToolAgent() {
		this((Material) null);
	}
	
	public ToolAgent(Material tool) {
		this(tool, null);
	}
	
	public ToolAgent(Material tool, Integer d) {
		this(tool == null ? null : tool.getId(), d);
	}
	
	public ToolAgent(Integer tool, Integer type) {
		id = tool;
		data = type;
	}
	
	public ToolAgent(ItemStack item) {
		this(item == null ? null : item.getTypeId(), item == null ? null : (int) item.getDurability());
	}

	private boolean isEqual(ToolAgent tool) {
		if(tool == null) return false;
		return id == tool.id && data == tool.data;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof ToolAgent)) return false;
		ToolAgent tool = (ToolAgent) other;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		if(!(other instanceof PlayerAgent)) return false;
		PlayerAgent tool = (PlayerAgent) other;
		if(id == null) return true;
		else if(data == null) return id == tool.getMaterialId();
		else return isEqual(tool.getTool());
	}
	
	@Override
	public Material getMaterial() {
		return Material.getMaterial(id);
	}

	@Override
	public int getMaterialId() {
		return id == null ? -1 : id;
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.PLAYER, id == null ? 0 : id, data == null ? 0 : data);
	}

	@Override
	public int getData() {
		return data == null ? -1 : data;
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	public static Agent parse(String name, String state) {
		Material mat = Material.getMaterial(name);
		if(mat == null) {
			if(name.equalsIgnoreCase("NOTHING")) mat = Material.AIR;
			else if(name.equalsIgnoreCase("DYE")) mat = Material.INK_SACK;
			else return null;
		}
		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ToolAgent(mat, d);
		} catch(NumberFormatException e) {}
		try {
			switch(mat) {
			case LOG:
			case LEAVES:
			case SAPLING:
				TreeSpecies species = TreeSpecies.valueOf(state);
				if(species != null) return new ToolAgent(mat, (int) species.getData());
				break;
			case WOOL:
				DyeColor wool = DyeColor.valueOf(state);
				if(wool != null) return new ToolAgent(mat, CommonMaterial.getWoolColor(wool));
				break;
			case INK_SACK:
				DyeColor dye = DyeColor.valueOf(state);
				if(dye != null) return new ToolAgent(mat, CommonMaterial.getDyeColor(dye));
				break;
			case COAL:
				CoalType coal = CoalType.valueOf(state);
				if(coal != null) return new ToolAgent(mat, (int) coal.getData());
				break;
			case DOUBLE_STEP:
			case STEP:
				Material step = Material.valueOf(state);
				if(step == null) throw new IllegalArgumentException("Unknown material " + state);
				switch(step) {
				case STONE:
					return new ToolAgent(mat, 0);
				case COBBLESTONE:
					return new ToolAgent(mat, 3);
				case SANDSTONE:
					return new ToolAgent(mat, 1);
				case WOOD:
					return new ToolAgent(mat, 2);
				default:
					throw new IllegalArgumentException("Illegal step material " + state);
				}
			default:
				if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + name + ": " + state);
			}
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		return new ToolAgent(mat);
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}
}
