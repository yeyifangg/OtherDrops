package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.MaterialOption;

@ConfigOnly(PlayerAgent.class)
public class ToolAgent extends Agent implements MaterialOption {
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
		super(ToolType.PLAYER);
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
	protected int getDataHash() {
		return id == null ? 0 : id;
	}
	
	@Override
	protected int getIdHash() {
		return data == null ? 0 : data;
	}

	@Override
	public int getData() {
		return data == null ? -1 : data;
	}
	
}
