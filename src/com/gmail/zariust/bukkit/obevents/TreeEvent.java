package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;

public class TreeEvent extends DropEvent {
	private boolean forceTree;
	private TreeType tree = TreeType.TREE;
	private static List<Material> tileEntities = Arrays.asList(Material.CHEST, Material.MOB_SPAWNER, Material.DISPENSER,
		Material.FURNACE, Material.NOTE_BLOCK, Material.SIGN_POST, Material.WALL_SIGN, Material.PISTON_EXTENSION,
		Material.PISTON_MOVING_PIECE, Material.JUKEBOX);
	
	public TreeEvent(TreeEvents source, boolean force) {
		super(force ? "FORCETREE" : "TREE", source);
		forceTree = force;
	}

	@Override
	public void executeAt(OccurredDrop event) {
		Location where = event.getLocation();
		Block block = where.getBlock().getRelative(BlockFace.DOWN);
		BlockState state = block.getState();
		if(forceTree && (!tileEntities.contains(state.getType()) || TreeEvents.forceOnTileEntities)) {
				block.setType(Material.DIRT);
		}
		// TODO: Is there any reason to allow the use of a BlockChangeDelegate here?
		where.getWorld().generateTree(where, tree);
		if(forceTree) state.update(true);
	}
	
	@Override
	public void interpretArguments(String... args) {
		if(args.length == 0) return;
		try {
			tree = TreeType.valueOf(args[0]);
			used(args[0]);
		} catch(IllegalArgumentException e) {
			// TODO: what to do if arguments illegal?
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		return true;
	}
	
}
