package com.gmail.zariust.bukkit.otherblocks.options.target;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class BlockTarget extends Target {
	private Material mat;
	private int data;
	
	public BlockTarget(Material type, byte d) {
		super(TargetType.BLOCK);
		mat = type;
		data = d;
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
}
