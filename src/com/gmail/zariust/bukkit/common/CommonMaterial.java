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
	
	public static short getWoolColor(DyeColor color) {
		switch (color) {
		case WHITE: return 0x0;
		case ORANGE: return 0x1;
		case MAGENTA: return 0x2;
		case LIGHT_BLUE: return 0x3;
		case YELLOW: return 0x4;
		case LIME: return 0x5;
		case PINK: return 0x6;
		case GRAY: return 0x7;
		case SILVER: return 0x8;
		case CYAN: return 0x9;
		case PURPLE: return 0xA;
		case BLUE: return 0xB;
		case BROWN: return 0xC;
		case GREEN: return 0xD;
		case RED: return 0xE;
		case BLACK: return 0xF;
		default: return 0xF;
		}
	}

	public static short getDyeColor(DyeColor color) {
		switch (color) {
		case WHITE: return 0xF;
		case ORANGE: return 0xE;
		case MAGENTA: return 0xD;
		case LIGHT_BLUE: return 0xC;
		case YELLOW: return 0xB;
		case LIME: return 0xA;
		case PINK: return 0x9;
		case GRAY: return 0x8;
		case SILVER: return 0x7;
		case CYAN: return 0x6;
		case PURPLE: return 0x5;
		case BLUE: return 0x4;
		case BROWN: return 0x3;
		case GREEN: return 0x2;
		case RED: return 0x1;
		case BLACK: return 0x0;
		default: return 0x0;
		}
	}

	@Deprecated
	public static short getAnyDataShort(String objectString, String enumValue) throws IllegalArgumentException {
		// Firstly, can the string be cast directly as a short?
		try {
			Short s = Short.parseShort(enumValue);
			return s;
		} catch(NumberFormatException ex) {}

		// If not, test the enum
		if (objectString.equalsIgnoreCase("LOG") || objectString.equalsIgnoreCase("LEAVES") || objectString.equalsIgnoreCase("SAPLING")) {	
			return (short) TreeSpecies.valueOf(enumValue).getData();
		} else if (objectString.equalsIgnoreCase("WOOL") || objectString.equalsIgnoreCase("CREATURE_SHEEP")) {
			return (short) DyeColor.valueOf(enumValue).getData();
		} else if (objectString.equalsIgnoreCase("INK_SACK")) {
			return (short) (0xF - DyeColor.valueOf(enumValue).getData());
		} else if (objectString.equalsIgnoreCase("COAL")) {
			return (short) (CoalType.valueOf(enumValue).getData());
		} else if (objectString.equalsIgnoreCase("CROPS")) {
			return (short) (CropState.valueOf(enumValue).getData());
		} else if (objectString.equalsIgnoreCase("STEP") || objectString.equalsIgnoreCase("DOUBLE_STEP")) {
			if(enumValue.equalsIgnoreCase("STONE")) return 0;
			else if(enumValue.equalsIgnoreCase("SANDSTONE")) return 1;
			else if(enumValue.equalsIgnoreCase("WOOD")) return 2;
			else if(enumValue.equalsIgnoreCase("COBBLESTONE")) return 3;
			else throw new IllegalArgumentException();
		} else if (objectString.equalsIgnoreCase("CREATURE_PIG")) {
			if(enumValue.equalsIgnoreCase("UNSADDLED")) return 0;
			else if(enumValue.equalsIgnoreCase("SADDLED")) return 1;
			else throw new IllegalArgumentException();
		} else if (objectString.equalsIgnoreCase("CREATURE_CREEPER")) {
			if(enumValue.equalsIgnoreCase("UNPOWERED")) return 0;
			else if(enumValue.equalsIgnoreCase("POWERED")) return 1;
			else throw new IllegalArgumentException();
		} else if (objectString.equalsIgnoreCase("CREATURE_WOLF")) {
			if(enumValue.equalsIgnoreCase("NEUTRAL")) return 0;
			else if(enumValue.equalsIgnoreCase("TAME")) return 1;
			else if(enumValue.equalsIgnoreCase("TAMED")) return 1;
			else if(enumValue.equalsIgnoreCase("ANGRY")) return 2;
			else throw new IllegalArgumentException();
		} else if (objectString.equalsIgnoreCase("CREATURE_SLIME")) {
			if(enumValue.equalsIgnoreCase("TINY")) return 1;
			else if(enumValue.equalsIgnoreCase("SMALL")) return 2;
			else if(enumValue.equalsIgnoreCase("BIG")) return 3;
			else if(enumValue.equalsIgnoreCase("HUGE")) return 4;
			else throw new IllegalArgumentException();
		} else {
			return 0;
		}
	}
}
