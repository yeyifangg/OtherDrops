package com.gmail.zariust.bukkit.otherblocks.options;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Vehicle;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class Target {
	public enum TargetType {BLOCK, CREATURE, PLAYER, GROUP, SPECIAL};
	public final static Target LEAF_DECAY = new Target(TargetType.SPECIAL);
	
	private TargetType type;
	private Material mat;
	private CreatureType creature;
	private int data;
	
	public Target(TargetType t) {
		
	}

	public Target(Block block) {
		this(TargetType.BLOCK);
		mat = block.getType();
		data = block.getData();
	}

	public Target(Entity entity) {
		this(TargetType.CREATURE);
		creature = CommonEntity.getCreatureType(entity);
		data = CommonEntity.getCreatureData(entity);
	}
	
	public Target(Painting painting) {
		// TODO: Also fetch what painting it is (no API for this yet)
		this(TargetType.BLOCK);
		this.mat = Material.PAINTING;
		this.data = 0;
	}
	
	public Target(Vehicle cart) {
		// TODO: Fetch whether a powered minecart is running? (no API for this yet?)
		this(TargetType.BLOCK);
		this.mat = CommonEntity.getVehicleType(cart);
	}
}
