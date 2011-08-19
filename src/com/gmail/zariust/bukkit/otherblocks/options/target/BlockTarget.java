package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.CoalType;
import org.bukkit.CropState;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.tool.ToolAgent;

public class BlockTarget implements Target {
	private int id;
	private int data;
	
	public BlockTarget(Material type, byte d) {
		this(type.getId(), d);
	}
	
	public BlockTarget(Block block) {
		this(block.getType(), block.getData());
	}
	
	public BlockTarget(Painting painting) {
		// TODO: Also fetch what painting it is (no API for this yet)
		this(Material.PAINTING, (byte) 0);
	}
	
	public BlockTarget(Vehicle cart) {
		// TODO: Fetch whether a powered minecart is running? (no API for this yet?)
		this(CommonEntity.getVehicleType(cart), (byte) 0);
	}

	public BlockTarget(int mat, int d) {
		this(mat, (byte) d);
	}
	
	public BlockTarget(int mat, byte d) {
		id = mat;
		data = d;
	}

	public BlockTarget(Material mat, int d) {
		this(mat, (byte) d);
	}

	public BlockTarget(Material mat) {
		this(mat, 0);
	}

	public BlockTarget() {
		// TODO: Well, this SHOULD be a wildcard, but at the moment it looks more like matching AIR...
	}

	public Material getMaterial() {
		return Material.getMaterial(id);
	}
	
	public int getId() {
		return id;
	}
	
	public int getData() {
		return data;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof BlockTarget)) return false;
		BlockTarget targ = (BlockTarget) other;
		return id == targ.id && data == targ.data;
	}
	
	@Override
	public int hashCode() {
		return (data << 16) | id;
	}

	@Override
	public boolean overrideOn100Percent() {
		return true;
	}

	@Override
	public ItemType getType() {
		return ItemType.BLOCK;
	}

	@Override
	public boolean matches(Target block) {
		return equals(block);
	}

	public static Target parse(String name, String state) {
		int id, val;
		try {
			id = Integer.parseInt(name);
			// TODO: Need some way to determine whether the ID is valid WITHOUT using only Material
			// Does ItemCraft have API for this?
		} catch(NumberFormatException x) {
			Material mat = Material.getMaterial(name);
			if(mat == null) return null;
			if(!mat.isBlock()) {
				// Only a very select few non-blocks are permitted as a target
				if(mat != Material.PAINTING && mat != Material.BOAT && mat != Material.MINECART &&
						mat != Material.POWERED_MINECART && mat != Material.STORAGE_MINECART)
					return null;
			}
			id = mat.getId();
		}
		try {
			val = Integer.parseInt(state);
			return new BlockTarget(id, val);
		} catch(NumberFormatException e) {}
		Material mat = Material.getMaterial(id);
		if(mat == null) return null;
		try {
			switch(mat) {
			case LOG:
			case LEAVES:
			case SAPLING:
				TreeSpecies species = TreeSpecies.valueOf(state);
				if(species != null) return new BlockTarget(mat, (int) species.getData());
				break;
			case WOOL:
				DyeColor wool = DyeColor.valueOf(state);
				if(wool != null) return new BlockTarget(mat, CommonMaterial.getWoolColor(wool));
				break;
			case INK_SACK:
				DyeColor dye = DyeColor.valueOf(state);
				if(dye != null) return new BlockTarget(mat, CommonMaterial.getDyeColor(dye));
				break;
			case COAL:
				CoalType coal = CoalType.valueOf(state);
				if(coal != null) return new BlockTarget(mat, (int) coal.getData());
				break;
			case DOUBLE_STEP:
			case STEP:
				Material step = Material.valueOf(state);
				if(step == null) throw new IllegalArgumentException("Unknown material " + state);
				switch(step) {
				case STONE:
					return new BlockTarget(mat, 0);
				case COBBLESTONE:
					return new BlockTarget(mat, 3);
				case SANDSTONE:
					return new BlockTarget(mat, 1);
				case WOOD:
					return new BlockTarget(mat, 2);
				default:
					throw new IllegalArgumentException("Illegal step material " + state);
				}
			case CROPS:
				CropState crops = CropState.valueOf(state);
				if(crops != null) return new BlockTarget(mat, (int) crops.getData());
				break;
				// TODO: Other blocks with data?
			case PAINTING:
				// TODO: Paintings? (needs API first)
				break;
			default:
				if(!state.isEmpty()) throw new IllegalArgumentException("Illegal data for " + name + ": " + state);
			}
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		return new BlockTarget(mat);
	}
}
