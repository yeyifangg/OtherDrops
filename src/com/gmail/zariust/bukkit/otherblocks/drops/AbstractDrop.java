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
	private Action action;
	
	protected static Random rng = new Random();

	public AbstractDrop(Target targ, Action act) {
		block = targ;
		action = act;
	}
	
	public abstract boolean matches(AbstractDrop other);
}
