package com.gmail.zariust.bukkit.common;

import java.util.List;
import java.util.Set;

import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.material.*;

public final class CommonMaterial {
	
	public static List<Material> getSynonymValues(String string) {
		return MaterialGroup.get(string).materials();
	}
	
	public static Set<String> getValidSynonyms() {
		return MaterialGroup.all();
	}
	
	public static boolean isValidSynonym(String string) {
		return MaterialGroup.isValid(string);
	}
	
	public static boolean isSynonymFor(String string, Material material) {
		if(!isValidSynonym(string)) return false;
		return getSynonymValues(string).contains(material);
	}
	
	// Colors
	public static int getWoolColor(DyeColor color) {
		return color.getData();
	}

	public static int getDyeColor(DyeColor color) {
		return 0xF - color.getData();
	}
	
	@SuppressWarnings("incomplete-switch")
	public static Integer parseBlockOrItemData(Material mat, String state) {
		switch(mat) {
		case LOG:
		case LEAVES:
		case SAPLING:
			TreeSpecies species = TreeSpecies.valueOf(state);
			if(species != null) return (int) species.getData();
			break;
		case WOOL:
			DyeColor wool = DyeColor.valueOf(state);
			if(wool != null) return getWoolColor(wool);
			break;
		case DOUBLE_STEP:
		case STEP:
			Material step = Material.valueOf(state);
			if(step == null) throw new IllegalArgumentException("Unknown material " + state);
			switch(step) {
			case STONE: return 0;
			case COBBLESTONE: return 3;
			case SANDSTONE: return 1;
			case WOOD: return 2;
			default:
				throw new IllegalArgumentException("Illegal step material " + state);
			}
		}
		return null;
	}

	public static Integer parseItemData(Material mat, String state) {
		if(mat.isBlock()) return parseBlockOrItemData(mat, state);
		switch(mat) {
		case INK_SACK:
			DyeColor dye = DyeColor.valueOf(state);
			if(dye != null) return getDyeColor(dye);
			break;
		case COAL:
			CoalType coal = CoalType.valueOf(state);
			if(coal != null) return (int) coal.getData();
			break;
		default:
			if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + mat + ": " + state);
		}
		return null;
	}
	
	@SuppressWarnings("incomplete-switch")
	public static String getBlockOrItemData(Material mat, int data) {
		switch(mat) {
		case LOG:
		case LEAVES:
		case SAPLING:
			return TreeSpecies.getByData((byte)data).toString();
		case WOOL:
			return DyeColor.getByData((byte)data).toString();
		case DOUBLE_STEP:
		case STEP:
			Step step = new Step(mat, (byte)data);
			return step.getMaterial().toString();
		}
		if(data > 0) return Integer.toString(data);
		return "";
	}
	
	@SuppressWarnings("incomplete-switch")
	public static String getItemData(Material mat, int data) {
		if(mat.isBlock()) return getBlockOrItemData(mat, data);
		switch(mat) {
		case COAL:
			return CoalType.getByData((byte)data).toString();
		case INK_SACK:
			return DyeColor.getByData((byte)(0xF - data)).toString();
		}
		if(data > 0) return Integer.toString(data);
		return "";
	}
}
