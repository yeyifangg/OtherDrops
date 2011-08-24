package com.gmail.zariust.bukkit.otherblocks.data;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RecordData implements Data {
	Material disc;
	
	public RecordData(Material record) {
		disc = record;
	}

	public RecordData(BlockState state) {
		// TODO: The Jukebox BlockSate is missing, so can't implement this yet
//		if(BlockState instanceof Jukebox) {
//			disc = ((Jukebox)state).getPlaying();
//		}
	}

	@Override
	public int getData() {
		return disc.getId();
	}
	
	@Override
	public void setData(int d) {
		disc = Material.getMaterial(d);
	}
	
	@Override
	public boolean matches(Data d) {
		if(!(d instanceof RecordData)) return false;
		return disc == ((RecordData)d).disc;
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat == Material.JUKEBOX || mat == Effect.RECORD_PLAY)
			return disc.toString();
		return "";
	}
	
	@Override
	public void setOn(BlockState state) {
		// TODO: The Jukebox BlockSate is missing, so can't implement this yet
//		if(!(state instanceof Jukebox)) {
//			OtherBlocks.logWarning("Tried to change a jukebox, but no jukebox was found!");
//			return;
//		}
//		((Jukebox)state).setPlaying(disc);
	}
	
	@Override // Jukeboxes are not entities, so nothing to do here
	public void setOn(Entity entity, Player witness) {}

	public static Data parse(String state) {
		Material mat = Material.getMaterial(state);
		if(mat == null || mat.getId() < 2256) return new SimpleData();
		return new RecordData(mat);
	}
	
}
