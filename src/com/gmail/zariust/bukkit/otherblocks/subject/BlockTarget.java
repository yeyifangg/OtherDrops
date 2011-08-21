package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.MaterialGroup;
import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;

public class BlockTarget implements Target {
	private int id;
	private int data; // TODO: Can't distinguish between LEAVES and LEAVES@GENERIC this way...
	
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
		Integer data = null;
		try {
			data = CommonMaterial.parseBlockData(mat, state);
		} catch(IllegalArgumentException e) {
			OtherBlocks.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new BlockTarget(mat, data);
		return new BlockTarget(mat);
	}
	
	@Override
	public String toString() {
		Material mat = Material.getMaterial(id);
		// TODO: What about the case where data is irrelevant?
		return mat + "@" + CommonMaterial.getBlockData(mat, data);
	}

	@Override
	public List<Target> canMatch() {
		// TODO: What about the any-block wildcard? (Probably should be ANY_BLOCK rather than ANY_OBJECT)
		//if( some condition ) return new BlocksTarget(MaterialGroup.ANY_OBJECT).canMatch();
		return Collections.singletonList((Target) this);
	}

	@Override
	public String getKey() {
		return Material.getMaterial(id).toString();
	}
}
