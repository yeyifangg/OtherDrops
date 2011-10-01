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

package com.gmail.zariust.common;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import static org.bukkit.Material.*;

import org.bukkit.TreeSpecies;
import org.bukkit.material.Step;

import static com.gmail.zariust.common.CommonPlugin.enumValue;

public enum CommonMaterial {
	GLASS_PANE(THIN_GLASS),
	WOODEN_SPADE(WOOD_SPADE), WOODEN_AXE(WOOD_AXE), WOODEN_HOE(WOOD_HOE), WOODEN_PICKAXE(WOOD_PICKAXE), WOODEN_SWORD(WOOD_SWORD),
	GOLDEN_SPADE(GOLD_SPADE), GOLDEN_AXE(GOLD_AXE), GOLDEN_HOE(GOLD_HOE), GOLDEN_PICKAXE(GOLD_PICKAXE), GOLDEN_SWORD(GOLD_SWORD),
	WOODEN_PLATE(WOOD_PLATE), PLANK(WOOD), WOODEN_PLANK(WOOD), WOODEN_DOOR(WOOD_DOOR),
	WOOD_DOOR_BLOCK(Material.WOODEN_DOOR), WOODEN_DOOR_BLOCK(Material.WOODEN_DOOR),
	STONE_PRESSUREPLATE(STONE_PLATE), WOOD_PRESSUREPLATE(WOOD_PLATE), WOODEN_PRESSUREPLATE(WOOD_PLATE),
	HANDS(AIR), HAND(AIR), NOTHING(AIR),
	TALL_GRASS(LONG_GRASS),
	DANDELION(YELLOW_FLOWER), ROSE(RED_ROSE), RED_FLOWER(RED_ROSE),
	MOSS_STONE(MOSSY_COBBLESTONE), MOSSY_COBBLE(MOSSY_COBBLESTONE),
	GUNPOWDER(SULPHUR), SULFUR(SULPHUR),
	TRAPDOOR(TRAP_DOOR),
	SLAB(STEP), DOUBLE_SLAB(DOUBLE_STEP),
	CRAFTING_TABLE(WORKBENCH),
	FARMLAND(SOIL),
	VINES(VINE),
	STONE_BRICK(SMOOTH_BRICK),
	DYE(INK_SACK),
	;
	private Material material;
	
	private CommonMaterial(Material mapTo) {
		material = mapTo;
	}
	
	public static Material matchMaterial(String mat) {
		// Aliases defined here override those in Material; the only example here is WOODEN_DOOR
		// You can remove it if you prefer not to break the occasional config file.
		// (I doubt many people assign drops to wooden doors, though, and including the BLOCK makes it less confusing.)
		Material material = enumValue(CommonMaterial.class, mat).material;
		if(material == null) material = Material.getMaterial(mat);
		return material;
	}
	
	// Colors
	public static int getWoolColor(DyeColor color) {
		return color.getData();
	}

	public static int getDyeColor(DyeColor color) {
		return 0xF - color.getData();
	}
	
	@SuppressWarnings("incomplete-switch")
	public static Integer parseBlockOrItemData(Material mat, String state) throws IllegalArgumentException {
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
		case SMOOTH_BRICK:
			if (state.equalsIgnoreCase("NORMAL")) return 0;
			if (state.equalsIgnoreCase("MOSSY")) return 1;
			if (state.equalsIgnoreCase("CRACKED")) return 2;
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
			case BRICK: return 4;
			case SMOOTH_BRICK: return 5;
			default:
				throw new IllegalArgumentException("Illegal step material " + state);
			}
		}
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public static String getBlockOrItemData(Material mat, int data) {
		switch(mat) {
		case LOG:
		case LEAVES:
		case SAPLING:
			return TreeSpecies.getByData((byte)((0x3) & data)).toString(); // (0x3) & data to remove leaf decay flag
		case WOOL:
			return DyeColor.getByData((byte)data).toString();
		case SMOOTH_BRICK:
			switch(data) {
			case 0: return "NORMAL";
			case 1: return "MOSSY";
			case 2: return "CRACKED";
			}
		case DOUBLE_STEP:
		case STEP:
			Step step = new Step(mat, (byte)data);
			return step.getMaterial().toString();
		}
		if(data > 0) return Integer.toString(data);
		return "";
	}
}
