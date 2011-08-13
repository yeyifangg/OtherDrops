package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;

import com.gmail.zariust.bukkit.otherblocks.options.*;
import com.sk89q.worldedit.regions.Region;

public class AbstractDrop {
	private String original;
	private List<Tool> tool;
	private List<Tool> toolExceptions;
	private List<World> worlds;
	private int damage;
	private double chance;
	private List<String> messages;
	private Time time;
	private List<Weather> weather;
	private List<Biome> biome;
	private List<DropEvent> event;
	private List<TreeType> eventTrees;
	private int height;
	private List<String> permissionGroups; // obseleted - use permissions
	private List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
	private List<String> permissions;
	private List<String> permissionsExcept;
	private String exclusive;
	private int delay;
	private List<Region> regions;
	private List<Material> replacementBlock;
	private String attackRange;
	private String lightLevel;
	private List<BlockFace> faces;
	private List<BlockFace> facesExcept;
	private List<String> commands;

	private Range<Integer> attackerDamage;
	
	protected static Random rng = new Random();
	
	private Location location; // not a configurable parameter - used for storing the location to use in performDrop();

	public AbstractDrop() {
		tool = new ArrayList<Tool>();
//		worlds = new ArrayList<String>();
//		messages = new ArrayList<String>();
//		weather = new ArrayList<String>();
//		biome = new ArrayList<String>();
//		event = new ArrayList<String>();
//		permissionGroups = new ArrayList<String>();
//		permissionGroupsExcept = new ArrayList<String>();
//		permissions = new ArrayList<String>();
//		permissionsExcept = new ArrayList<String>();
		//regions = new ArrayList<String>();
		
	}

	// Attacker Damage
	public int getRandomAttackerDamage()
	{
		if (attackerDamage.getMin() == attackerDamage.getMax()) return attackerDamage.getMin();
		
		int randomVal = (attackerDamage.getMin() + rng.nextInt(attackerDamage.getMax() - attackerDamage.getMin() + 1));
		return randomVal;
	}

	public void setAttackerDamage(int val) {
		attackerDamage = new Range<Integer>(val, val);
	}
	
	public void setAttackerDamage(Integer low, Integer high) {
		attackerDamage = new Range<Integer>(low, high);
	}
	
//	public boolean isAttackerDamageValid(Short test) {
//		if(this.attackerDamageMin == null) return true;
//		return (test >= this.attackerDamageMin && test <= this.attackerDamageMax);
//	}
	
}
