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

public abstract class AbstractDrop {
	private Target block;
	private List<Tool> tool;
	private List<Tool> toolExceptions;
	private List<World> worlds;
	private List<Region> regions;
	private List<Weather> weather;
	private List<BlockFace> faces;
	private List<BlockFace> facesExcept;
	private List<Biome> biome;
	private Time time;
	private List<String> permissionGroups; // obseleted - use permissions
	private List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
	private List<String> permissions;
	private List<String> permissionsExcept;
	private Height height;
	private String attackRange;
	private String lightLevel;
	
	protected static Random rng = new Random();
	
//	private Location location; // not a configurable parameter - used for storing the location to use in performDrop();

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
}
