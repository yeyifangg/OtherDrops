package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.common.CommonMaterial;

import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemData implements Data, RangeableData {
	int data;
	
	public ItemData(int d) {
		data = d;
	}

	public ItemData(ItemStack item) {
		data = item.getDurability();
	}

	@Override
	public int getData() {
		return data;
	}
	
	@Override
	public void setData(int d) {
		data = d;
	}
	
	@Override
	public boolean matches(Data d) {
		return data == d.getData();
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat instanceof Material) return get((Material)mat);
		return "";
	}
	
	@SuppressWarnings("incomplete-switch")
	private String get(Material mat) {
		if(mat.isBlock()) return CommonMaterial.getBlockOrItemData(mat, data);
		switch(mat) {
		case COAL:
			return CoalType.getByData((byte)data).toString();
		case INK_SACK:
			return DyeColor.getByData((byte)(0xF - data)).toString();
		}
		if(data > 0) return Integer.toString(data);
		return "";
	}
	
	@Override // Items aren't blocks, so nothing to do here
	public void setOn(BlockState state) {}
	
	@Override // Items aren't entities, so nothing to do here
	public void setOn(Entity entity, Player witness) {}

	public static Data parse(Material mat, String state) {
		if(state.startsWith("RANGE")) return RangeData.parse(state);
		Integer data = 0;
		if(mat.isBlock()) {
			data = CommonMaterial.parseBlockOrItemData(mat, state);
		} else switch(mat) {
		case INK_SACK:
			DyeColor dye = DyeColor.valueOf(state);
			if(dye != null) data = CommonMaterial.getDyeColor(dye);
			break;
		case COAL:
			CoalType coal = CoalType.valueOf(state);
			if(coal != null) data = Integer.valueOf(coal.getData());
			break;
		default:
			if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + mat + ": " + state);
		}
		return (data == null) ? null : new ItemData(data);
	}
	
	public String toString() {
		return String.valueOf(data);
	}
}
