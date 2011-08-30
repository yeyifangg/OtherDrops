package com.gmail.zariust.odspecials;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;

public class TreeEvent extends SpecialResult {
	private boolean forceTree;
	private TreeType tree = TreeType.TREE;
	private static List<Material> tileEntities = Arrays.asList(Material.CHEST, Material.MOB_SPAWNER, Material.DISPENSER,
		Material.FURNACE, Material.BURNING_FURNACE, Material.NOTE_BLOCK, Material.SIGN_POST, Material.WALL_SIGN,
		Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.JUKEBOX);
	
	public TreeEvent(TreeEvents source, boolean force) {
		super(force ? "FORCETREE" : "TREE", source);
		forceTree = force;
	}

	@Override
	public void executeAt(OccurredDropEvent event) {
		Location where = event.getLocation().clone(); // clone, just in case we want to modify the location later
		OtherDrops.logInfo("Event (trees): generating tree. Force="+forceTree+". Block at 'root' location is: "+where.clone().add(0, -1, 0).getBlock().getType().toString(),HIGHEST);
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
	public void interpretArguments(List<String> args) {
		for(String arg : args) {
			try {
				tree = TreeType.valueOf(arg);
				used(arg);
			} catch(IllegalArgumentException e) {}
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDropEvent drop) {
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDropEvent drop) {
		return true;
	}
	
}
