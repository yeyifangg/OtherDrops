package com.gmail.zariust.otherdrops.subject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.options.ConfigOnly;

@ConfigOnly(PlayerSubject.class)
public class ToolAgent implements Agent {
	private Material id;
	private Data data;
	
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
	
	public ToolAgent(Material tool, Data d) {
		id = tool;
		data = d;
	}

	private boolean isEqual(ToolAgent tool) {
		if(tool == null) return false;
		if (id == null) return true; // null means ANY_OBJECT
		return id == tool.id && data.equals(tool.data);
	}

	private boolean isMatch(ToolAgent tool) {
		if(tool == null) return false;
		return id == tool.id && data.matches(tool.data);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof ToolAgent)) return false;
		ToolAgent tool = (ToolAgent) other;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Subject other) {
		// example of data passed:
		// this: id=DIAMOND_SPADE, data=null (data is null unless specified in the config)
		// other=PLAYER@Xarqn with DIAMOND_SPADE@4

		// Only players can hold & use tools - fail match if not a PlayerSubject
		if(!(other instanceof PlayerSubject)) return false;
		// Find the tool that the player is holding
		PlayerSubject tool = (PlayerSubject) other;

		OtherDrops.logInfo("tool agent check : id="+id.toString()+" gettool="+tool.getTool() + " material="+tool.getMaterial() + " id=mat:"+(id==tool.getMaterial()), 5);

		if(id == null) return true;
		else if(data == null) return id == tool.getMaterial();
		else return isMatch(tool.getTool());
	}
	
	public Material getMaterial() {
		return id;
	}

	@Override
	public int hashCode() {
		return AbstractDropEvent.hashCode(ItemCategory.PLAYER, id == null ? 0 : id.getId(), data == null ? 0 : data.getData());
	}

	public int getData() {
		return data == null ? -1 : data.getData();
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.PLAYER;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	public static Agent parse(String name, String state) {
		name = name.toUpperCase();
		state = state.toUpperCase();
		Material mat = Material.getMaterial(name);
		if(mat == null) {
			if(name.equalsIgnoreCase("NOTHING")) mat = Material.AIR;
			else if(name.equalsIgnoreCase("DYE")) mat = Material.INK_SACK;
			else {
				OtherDrops.logInfo("Unrecognized tool: "+name+"@"+state,3);
				return null;
			}
		}

		// If "state" is empty then no data defined, make sure we don't use 0 as data otherwise later matching fails
		if (state.isEmpty()) return new ToolAgent(mat);

		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ToolAgent(mat, d);
		} catch(NumberFormatException e) {}
		Data data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			OtherDrops.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ToolAgent(mat, data);
		return new ToolAgent(mat);
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}

	@Override
	public String toString() {
		if(id == null) return "ANY_OBJECT";
		String ret = id.toString();
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(id);
		return ret;
	}
}
