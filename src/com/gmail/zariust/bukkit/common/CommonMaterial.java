package com.gmail.zariust.bukkit.common;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.CoalType;
import org.bukkit.CropState;
import org.bukkit.DyeColor;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
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
		state = state.toUpperCase();
		switch(mat) {
		case LOG: case LEAVES: case SAPLING: case WOOL: case DOUBLE_STEP: case STEP:
			return parseBlockItemData(mat, state);
		case CROPS:
			CropState crops = CropState.valueOf(state);
			if(crops != null) return (int) crops.getData();
			break;
		case LONG_GRASS:
			GrassSpecies grass = GrassSpecies.valueOf(state);
			if(grass != null) return (int) grass.getData();
			break;
		// Blocks whose only attribute is direction
		case FURNACE:
		case BURNING_FURNACE:
		case DISPENSER:
			FurnaceAndDispenser fd = new FurnaceAndDispenser(mat);
			fd.setFacingDirection(BlockFace.valueOf(state));
			return (int)fd.getData();
		case LADDER:
			Ladder ladder = new Ladder(mat);
			ladder.setFacingDirection(BlockFace.valueOf(state));
			return (int)ladder.getData();
		case PUMPKIN:
		case JACK_O_LANTERN:
			Pumpkin pumpkin = new Pumpkin(mat);
			pumpkin.setFacingDirection(BlockFace.valueOf(state));
			return (int)pumpkin.getData();
		case SIGN_POST:
		case WALL_SIGN:
			Sign sign = new Sign(mat);
			sign.setFacingDirection(BlockFace.valueOf(state));
			return (int)sign.getData();
		case WOOD_STAIRS:
		case COBBLESTONE_STAIRS:
			Stairs stairs = new Stairs(mat);
			stairs.setFacingDirection(BlockFace.valueOf(state));
			return (int)stairs.getData();
		case TORCH:
			Torch torch = new Torch(mat);
			torch.setFacingDirection(BlockFace.valueOf(state));
			return (int)torch.getData();
		case REDSTONE_TORCH_OFF:
		case REDSTONE_TORCH_ON:
			RedstoneTorch invert = new RedstoneTorch(mat);
			invert.setFacingDirection(BlockFace.valueOf(state));
			return (int)invert.getData();
		// Powerable blocks
		case LEVER:
			Lever lever = new Lever(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) lever.setPowered(true);
				else lever.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)lever.getData();
		case STONE_BUTTON:
			Button button = new Button(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) button.setPowered(true);
				else button.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)button.getData();
		// Pistons (overlaps with previous)
		case PISTON_BASE:
		case PISTON_STICKY_BASE:
			PistonBaseMaterial piston = new PistonBaseMaterial(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) piston.setPowered(true);
				else piston.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)piston.getData();
		case PISTON_EXTENSION:
			PistonExtensionMaterial pistonHead = new PistonExtensionMaterial(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("STICKY")) pistonHead.setSticky(true);
				else pistonHead.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)pistonHead.getData();
		// Rails
		case RAILS:
			Rails rail = new Rails(mat);
			{
				boolean slope = false;
				BlockFace face = BlockFace.NORTH;
				for(String arg : state.split("/")) {
					if(arg.equals("SLOPE")) slope = true;
					else face = BlockFace.valueOf(arg);
				}
				rail.setDirection(face, slope);
			}
			return (int)rail.getData();
		case POWERED_RAIL:
			PoweredRail booster = new PoweredRail(mat);
			{
				boolean slope = false;
				BlockFace face = BlockFace.NORTH;
				for(String arg : state.split("/")) {
					if(arg.equals("SLOPE")) slope = true;
					else if(arg.equals("POWERED")) booster.setPowered(true);
					else face = BlockFace.valueOf(arg);
				}
				booster.setDirection(face, slope);
			}
			return (int)booster.getData();
		// Pressable blocks (overlaps with previous)
		case DETECTOR_RAIL:
			DetectorRail detector = new DetectorRail(mat);
			{
				boolean slope = false;
				BlockFace face = BlockFace.NORTH;
				for(String arg : state.split("/")) {
					if(arg.equals("SLOPE")) slope = true;
					else if(arg.equals("PRESSED")) detector.setPressed(true);
					else face = BlockFace.valueOf(arg);
				}
				detector.setDirection(face, slope);
			}
			return (int)detector.getData();
		// Misc
		case BED_BLOCK:
			Bed bed = new Bed(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("HEAD")) bed.setHeadOfBed(true);
				else bed.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)bed.getData();
		case CAKE_BLOCK:
			Cake cake = new Cake(mat);
			if(state.startsWith("EATEN-"))
				cake.setSlicesEaten(Integer.parseInt(state.substring(6)));
			else if(state.startsWith("LEFT-"))
				cake.setSlicesRemaining(Integer.parseInt(state.substring(5)));
			else if(state.equals("FULL")) return 0;
			return (int)cake.getData();
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
			Diode diode = new Diode(mat);
			for(String arg : state.split("/")) {
				if(arg.matches("[1-4]")) diode.setDelay(Integer.parseInt(arg));
				else diode.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)diode.getData();
		case TRAP_DOOR:
			TrapDoor hatch = new TrapDoor(mat);
			for(String arg : state.split("/")) {
				// TODO: Should use a setOpen method, but there isn't one...
				if(arg.equals("OPEN")) hatch.setData((byte)(hatch.getData() | 0x4));
				else hatch.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)hatch.getData();
		case WOODEN_DOOR:
		case IRON_DOOR_BLOCK:
			Door door = new Door(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("OPEN")) door.setOpen(true);
				else if(arg.equals("TOP")) door.setTopHalf(true);
				else door.setFacingDirection(BlockFace.valueOf(arg));
			}
			return (int)door.getData();
		// Tile entities
		case MOB_SPAWNER:
			return CreatureType.fromName(state).ordinal();
		case NOTE_BLOCK:
			if(!state.matches("([A-G])(#?)([0-2]?)")) break;
			Note.Tone tone = Note.Tone.valueOf(state.substring(0, 1));
			if(tone == null) break;
			byte octave;
			if(state.matches("..?[0-2]")) octave = Byte.parseByte(state.substring(state.length() - 1));
			else octave = 1;
			Note note = new Note(octave, tone, state.contains("#"));
			return (int)note.getId();
		// Paintings and vehicles
		case PAINTING:
			// TODO: Needs API first
			break;
		case BOAT:
			if(state.equals("PLAYER")) return -2;
			else if(state.equals("OCCUPIED")) return -1;
			else if(state.equals("FULL")) return 0;
			break;
		case MINECART: // -2 player, -1 full, 0 empty
			if(state.equals("PLAYER")) return -2;
			else if(state.equals("OCCUPIED")) return -1;
			else if(state.equals("FULL")) return 0;
			return CreatureType.fromName(state).ordinal();
		case POWERED_MINECART:
			break;
		case STORAGE_MINECART:
			return Material.getMaterial(state).getId();
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

	@SuppressWarnings("incomplete-switch")
	public static String getBlockData(Material mat, int data) {
		String result = "";
		switch(mat) {
		// Simple enum-based blocks
		case LOG:
		case LEAVES:
		case SAPLING:
			return TreeSpecies.getByData((byte)data).toString();
		case WOOL:
			return DyeColor.getByData((byte)data).toString();
		case CROPS:
			return CropState.getByData((byte)data).toString();
		case LONG_GRASS:
			return GrassSpecies.getByData((byte)data).toString();
		// Blocks whose only attribute is direction
		case FURNACE:
		case BURNING_FURNACE:
		case DISPENSER:
			// TODO: These are also tile entities
			FurnaceAndDispenser fd = new FurnaceAndDispenser(mat, (byte)data);
			return fd.getFacing().toString();
		case LADDER:
			Ladder ladder = new Ladder(mat, (byte)data);
			return ladder.getFacing().toString();
		case PUMPKIN:
		case JACK_O_LANTERN:
			Pumpkin pumpkin = new Pumpkin(mat, (byte)data);
			return pumpkin.getFacing().toString();
		case SIGN_POST:
		case WALL_SIGN:
			Sign sign = new Sign(mat, (byte)data);
			return sign.getFacing().toString();
		case WOOD_STAIRS:
		case COBBLESTONE_STAIRS:
			Stairs stairs = new Stairs(mat, (byte)data);
			return stairs.getFacing().toString();
		case TORCH:
			Torch torch = new Torch(mat, (byte)data);
			return torch.getFacing().toString();
		case REDSTONE_TORCH_OFF:
		case REDSTONE_TORCH_ON:
			RedstoneTorch invert = new RedstoneTorch(mat, (byte)data);
			result += invert.getFacing();
			break;
		// Powerable blocks
		case LEVER:
			Lever lever = new Lever(mat, (byte)data);
			if(lever.isPowered()) result += "POWERED/";
			result += lever.getFacing();
			break;
		case STONE_BUTTON:
			Button button = new Button(mat, (byte)data);
			if(button.isPowered()) result += "POWERED/";
			result += button.getFacing();
			break;
		// Pistons (overlaps with previous)
		case PISTON_BASE:
		case PISTON_STICKY_BASE:
			PistonBaseMaterial piston = new PistonBaseMaterial(mat, (byte)data);
			if(piston.isPowered()) result += "POWERED/";
			result += piston.getFacing();
			break;
		case PISTON_EXTENSION:
			PistonExtensionMaterial pistonHead = new PistonExtensionMaterial(mat, (byte)data);
			if(pistonHead.isSticky()) result += "STICKY/";
			result += pistonHead.getFacing();
			break;
		// Rails
		case RAILS:
			Rails rail = new Rails(mat, (byte)data);
			if(rail.isOnSlope()) result += "SLOPE/";
			result += rail.getDirection();
			break;
		case POWERED_RAIL:
			PoweredRail booster = new PoweredRail(mat, (byte)data);
			if(booster.isPowered()) result += "POWERED/";
			if(booster.isOnSlope()) result += "SLOPE/";
			result += booster.getDirection();
			break;
		// Pressable blocks (overlaps with previous)
		case DETECTOR_RAIL:
			DetectorRail detector = new DetectorRail(mat, (byte)data);
			if(detector.isPressed()) result += "PRESSED/";
			if(detector.isOnSlope()) result += "SLOPE/";
			result += detector.getDirection();
			break;
		// Misc
		case BED_BLOCK:
			Bed bed = new Bed(mat, (byte)data);
			if(bed.isHeadOfBed()) result += "HEAD/";
			result += bed.getFacing();
			break;
		case CAKE_BLOCK:
			Cake cake = new Cake(mat, (byte)data);
			result += "EATEN-";
			result += cake.getSlicesEaten();
			break;
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
			Diode diode = new Diode(mat, (byte)data);
			result += diode.getDelay();
			result += "/";
			result += diode.getFacing();
			break;
		case STEP:
		case DOUBLE_STEP:
			Step step = new Step(mat, (byte)data);
			return step.getMaterial().toString();
		case TRAP_DOOR:
			TrapDoor hatch = new TrapDoor(mat, (byte)data);
			if(hatch.isOpen()) result += "OPEN/";
			result += hatch.getFacing();
			break;
		case WOODEN_DOOR:
		case IRON_DOOR_BLOCK:
			Door door = new Door(mat, (byte)data);
			if(door.isTopHalf()) result += "TOP/";
			if(door.isOpen()) result += "OPEN/";
			result += door.getFacing();
			break;
		// Items (here for completeness, unlikely to be reached)
		case COAL:
			return CoalType.getByData((byte)data).toString();
		case INK_SACK:
			return DyeColor.getByData((byte)(0xF - data)).toString();
		// Tile entities
		case MOB_SPAWNER:
			return CreatureType.values()[data].toString();
		case NOTE_BLOCK:
			Note note = new Note((byte)data);
			result += note.getTone();
			if(note.isSharped()) result += "#";
			result += note.getOctave();
			break;
		case CHEST:
			return Material.getMaterial(data).toString();
		// Paintings and vehicles
		case PAINTING:
			// TODO: Needs API first
			break;
		case BOAT:
			if(data == -2) return "PLAYER";
			break;
		case MINECART: // -2 player, -1 full, 0 empty
			if(data == -2) return "PLAYER";
			else if(data == -1) return "OCCUPIED";
			else if(data == 0) return "EMPTY";
			else if(data > 0)
				return CreatureType.values()[data - 1].toString();
			break;
		case POWERED_MINECART:
			break;
		case STORAGE_MINECART:
			return Material.getMaterial(data).toString();
		}
		if(result.isEmpty() && data > 0) return Integer.toString(data);
		return result;
	}
}
