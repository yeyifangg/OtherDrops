// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.subject;

import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.options.ConfigOnly;
import com.gmail.zariust.otherdrops.options.ToolDamage;

@ConfigOnly(PlayerSubject.class)
public class ToolAgent implements Agent {
	private ItemStack actualTool;
	private Material id;
	private Data data;
	private Map<Enchantment, Integer> enchantments;

	public ToolAgent() {
		this((Material) null);
	}
	
	public ToolAgent(Material tool) {
		this(tool, null);
	}
	
	public ToolAgent(Material tool, int d, Map<Enchantment, Integer> enchantment) {
		this(tool, new ItemData(d));
		enchantments = enchantment;
	}

	public ToolAgent(Material tool, int d) {
		this(tool, new ItemData(d));
	}
	
	public ToolAgent(ItemStack item) {
		this(item == null ? null : item.getType(), item == null ? null : new ItemData(item));

		actualTool = item;
	}
	
	public ToolAgent(Material tool, Data d, Map<Enchantment, Integer> enchString) {
		id = tool;
		data = d;
		enchantments = enchString;
	}

	public ToolAgent(Material tool, Data d) {
		id = tool;
		data = d;
	}

	private boolean isEqual(ToolAgent tool) {
		if(tool == null) return false;
		if (id == null) return true; // null means ANY_OBJECT
		if (data == null) return (id == tool.id); // no data to check (wildcard) so just check id versus tool.id 
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

		OtherDrops.logInfo("tool agent check : id="+id.toString()+" gettool="+tool.getTool() + " material="+tool.getMaterial() + " id=mat:"+(id==tool.getMaterial()), Verbosity.EXTREME);
		if (!enchantments.isEmpty()) {
			boolean match = false;
			match = CommonEnchantments.matches(enchantments, tool.getTool().actualTool.getEnchantments());
			if (!match) return false;
		}

		if(id == null) return true;
		else if(data == null) return id == tool.getMaterial();
		else return isMatch(tool.getTool());
	}
	
	public Material getMaterial() {
		return id;
	}

	@Override
	public int hashCode() {
		return new HashCode(this).get(id);
	}

	@Override
	public Data getData() {
		return data;
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
		return parse(name, state, "");
	}
	
	public static Agent parse(String name, String state, String enchantments) {
		name = name.toUpperCase();
		state = state.toUpperCase();
		Material mat = CommonMaterial.matchMaterial(name);
		if(mat == null) {
			OtherDrops.logInfo("Unrecognized tool: "+name+(state.isEmpty()?"":"@"+state),HIGHEST);
			return null;
		}


		Map <Enchantment, Integer> enchPass = CommonEnchantments.parseEnchantments(enchantments);


		// If "state" is empty then no data defined, make sure we don't use 0 as data otherwise later matching fails
		if (state.isEmpty()) return new ToolAgent(mat, null, enchPass);

		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ToolAgent (mat, d, enchPass);
		} catch(NumberFormatException e) {}
		Data data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			OtherDrops.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ToolAgent(mat, data, enchPass);
		return new ToolAgent(mat, null, enchPass);
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(ToolDamage amount, Random rng) {}

	@Override
	public String toString() {
		if(id == null) return "ANY_OBJECT";
		String ret = id.toString();
		// TODO: Will data ever be null, or will it just be 0?
		if(data != null) ret += "@" + data.get(id);
		return ret;
	}

	@Override
	public String getReadableName() {
		if(id == null) return "ANY_OBJECT";
		return id.toString();
	}

}
