package com.gmail.zariust.bukkit.common;

import java.util.*;

import org.bukkit.*;

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
	
	private static Integer parseBlockItemData(Material mat, String state) {
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

	public static Integer parseBlockData(Material mat, String state) {
		switch(mat) {
		case LOG: case LEAVES: case SAPLING: case WOOL: case DOUBLE_STEP: case STEP:
			return parseBlockItemData(mat, state);
		case CROPS:
			CropState crops = CropState.valueOf(state);
			if(crops != null) return (int) crops.getData();
			break;
			// TODO: Other blocks with data?
		case PAINTING:
			// TODO: Paintings? (needs API first)
			break;
		default:
			if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + mat + ": " + state);
		}
		return null;
	}

	public static Integer parseItemData(Material mat, String state) {
		switch(mat) {
		case LOG: case LEAVES: case SAPLING: case WOOL: case DOUBLE_STEP: case STEP:
			return parseBlockItemData(mat, state);
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

	public static String getBlockData(Material mat, int data) {
		// TODO: Actually fetch the data properly
		return Integer.toString(data);
	}
}
