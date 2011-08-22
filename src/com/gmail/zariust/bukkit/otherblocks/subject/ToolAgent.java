package com.gmail.zariust.bukkit.otherblocks.subject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.data.ItemData;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

@ConfigOnly(PlayerSubject.class)
public class ToolAgent implements Agent {
	private Material id;
	private ItemData data;
	
	public ToolAgent() {
		this((Material) null);
	}
	
	public ToolAgent(Material tool) {
		this(tool, null);
	}
	
	public ToolAgent(Material tool, int d) {
		this(tool, new ItemData(d));
	}
	
	public ToolAgent(ItemStack item) {
		this(item == null ? null : item.getType(), item == null ? null : new ItemData(item));
	}
	
	public ToolAgent(Material tool, ItemData d) {
		id = tool;
		data = d;
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
	public boolean matches(Subject other) {
		if(!(other instanceof PlayerSubject)) return false;
		PlayerSubject tool = (PlayerSubject) other;
		if(id == null) return true;
		else if(data == null) return id == tool.getMaterial();
		else return isEqual(tool.getTool());
	}
	
	public Material getMaterial() {
		return id;
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.PLAYER, id == null ? 0 : id.getId(), data == null ? 0 : data.getData());
	}

	public int getData() {
		return data == null ? -1 : data.getData();
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	@Override
	public Location getLocation() {
		return null;
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
		ItemData data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ToolAgent(mat, data);
		return new ToolAgent(mat);
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}
}
