package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

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
}
