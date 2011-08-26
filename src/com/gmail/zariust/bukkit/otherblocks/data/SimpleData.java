package com.gmail.zariust.bukkit.otherblocks.data;

import com.gmail.zariust.bukkit.common.CommonMaterial;

import org.bukkit.CropState;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.*;

public class SimpleData implements Data, RangeableData {
	private int data;
	
	public SimpleData(byte d) {
		data = d;
	}

	public SimpleData() {
		this((byte)0);
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
	public void setOn(BlockState state) {
		MaterialData mat = new MaterialData(state.getType(), (byte)data);
		state.setData(mat);
	}
	
	@Override
	public void setOn(Entity entity, Player witness) {
		// TODO: Eventually this is where painting data will be handled
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat instanceof Material) return get((Material)mat);
		return "";
	}
	
	@SuppressWarnings("incomplete-switch")
	private String get(Material mat) {
		String result = "";
		switch(mat) {
		// Simple enum-based blocks
		case CROPS:
			return CropState.getByData((byte)data).toString();
		case LONG_GRASS:
			return GrassSpecies.getByData((byte)data).toString();
		// Blocks whose only attribute is direction
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
		}
		if(result.isEmpty()) return CommonMaterial.getBlockOrItemData(mat, data);
		return result;
	}

	public static Data parse(Material mat, String state) {
		if(state == null || state.isEmpty()) return null;
		if(state.startsWith("RANGE")) return RangeData.parse(state);
		state = state.toUpperCase();
		int ret = -1;
		switch(mat) {
		case LOG: case LEAVES: case SAPLING: case WOOL: case DOUBLE_STEP: case STEP:
			Integer data = CommonMaterial.parseBlockOrItemData(mat, state);
			if(data != null) ret = data;
			break;
		case CROPS:
			CropState crops = CropState.valueOf(state);
			if(crops != null) ret = crops.getData();
			break;
		case LONG_GRASS:
			GrassSpecies grass = GrassSpecies.valueOf(state);
			if(grass != null) ret = grass.getData();
			break;
		// Blocks whose only attribute is direction
		case LADDER:
			Ladder ladder = new Ladder(mat);
			ladder.setFacingDirection(BlockFace.valueOf(state));
			ret = ladder.getData();
			break;
		case PUMPKIN:
		case JACK_O_LANTERN:
			Pumpkin pumpkin = new Pumpkin(mat);
			pumpkin.setFacingDirection(BlockFace.valueOf(state));
			ret = pumpkin.getData();
			break;
		case SIGN_POST: // TODO: Should we allow sign text matching?
		case WALL_SIGN:
			Sign sign = new Sign(mat);
			sign.setFacingDirection(BlockFace.valueOf(state));
			ret = sign.getData();
			break;
		case WOOD_STAIRS:
		case COBBLESTONE_STAIRS:
			Stairs stairs = new Stairs(mat);
			stairs.setFacingDirection(BlockFace.valueOf(state));
			ret = stairs.getData();
			break;
		case TORCH:
			Torch torch = new Torch(mat);
			torch.setFacingDirection(BlockFace.valueOf(state));
			ret = torch.getData();
			break;
		case REDSTONE_TORCH_OFF:
		case REDSTONE_TORCH_ON:
			RedstoneTorch invert = new RedstoneTorch(mat);
			invert.setFacingDirection(BlockFace.valueOf(state));
			ret = invert.getData();
			break;
		// Powerable blocks
		case LEVER:
			Lever lever = new Lever(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) lever.setPowered(true);
				else lever.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = lever.getData();
			break;
		case STONE_BUTTON:
			Button button = new Button(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) button.setPowered(true);
				else button.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = button.getData();
			break;
		// Pistons (overlaps with previous)
		case PISTON_BASE:
		case PISTON_STICKY_BASE:
			PistonBaseMaterial piston = new PistonBaseMaterial(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("POWERED")) piston.setPowered(true);
				else piston.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = piston.getData();
			break;
		case PISTON_EXTENSION:
			PistonExtensionMaterial pistonHead = new PistonExtensionMaterial(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("STICKY")) pistonHead.setSticky(true);
				else pistonHead.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = pistonHead.getData();
			break;
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
			ret = rail.getData();
			break;
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
			ret = booster.getData();
			break;
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
			ret = detector.getData();
			break;
		// Misc
		case BED_BLOCK:
			Bed bed = new Bed(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("HEAD")) bed.setHeadOfBed(true);
				else bed.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = bed.getData();
			break;
		case CAKE_BLOCK:
			Cake cake = new Cake(mat);
			if(state.startsWith("EATEN-"))
				cake.setSlicesEaten(Integer.parseInt(state.substring(6)));
			else if(state.startsWith("LEFT-"))
				cake.setSlicesRemaining(Integer.parseInt(state.substring(5)));
			else if(state.equals("FULL")) cake.setSlicesEaten(0);
			ret = cake.getData();
			break;
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
			Diode diode = new Diode(mat);
			for(String arg : state.split("/")) {
				if(arg.matches("[1-4]")) diode.setDelay(Integer.parseInt(arg));
				else diode.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = diode.getData();
			break;
		case TRAP_DOOR:
			TrapDoor hatch = new TrapDoor(mat);
			for(String arg : state.split("/")) {
				// TODO: Should use a setOpen method, but there isn't one...
				if(arg.equals("OPEN")) hatch.setData((byte)(hatch.getData() | 0x4));
				else hatch.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = hatch.getData();
			break;
		case WOODEN_DOOR:
		case IRON_DOOR_BLOCK:
			Door door = new Door(mat);
			for(String arg : state.split("/")) {
				if(arg.equals("OPEN")) door.setOpen(true);
				else if(arg.equals("TOP")) door.setTopHalf(true);
				else door.setFacingDirection(BlockFace.valueOf(arg));
			}
			ret = door.getData();
			break;
		// Tile entities
		case FURNACE:
		case BURNING_FURNACE:
		case DISPENSER:
		case CHEST:
			return ContainerData.parse(mat, state);
		case MOB_SPAWNER:
			return SpawnerData.parse(state);
		case NOTE_BLOCK:
			return NoteData.parse(state);
		case JUKEBOX:
			return RecordData.parse(state);
		// Paintings and vehicles
		case PAINTING:
			// TODO: Needs API first
			break;
		case BOAT:
		case MINECART:
		case POWERED_MINECART:
			return VehicleData.parse(mat, state);
		case STORAGE_MINECART:
			return ContainerData.parse(mat, state);
		default:
			if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + mat + ": " + state);
		}
		return new SimpleData((byte)ret);
	}
}
