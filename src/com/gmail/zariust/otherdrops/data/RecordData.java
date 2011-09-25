package com.gmail.zariust.otherdrops.data;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.OtherDrops;

public class RecordData extends EffectData {
	private Material disc;
	
	public RecordData(Material record) {
		super(64);
		disc = record;
	}

	public RecordData(BlockState state) {
		super(64);
		if(state instanceof Jukebox) {
			disc = ((Jukebox)state).getPlaying();
		}
	}

	@Override
	public int getData() {
		Integer discId = null;
		if (disc == null) {
			// if you don't specify a valid record you just get a random one
			if (OtherDrops.rng.nextInt() > 0.5) {
				discId = Material.GREEN_RECORD.getId();
			} else {
				discId = Material.GOLD_RECORD.getId();
			}			
		} else {
			discId = disc.getId();
		}

		return discId;
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
		String discName = "";
		if (disc != null) discName = disc.toString();
		if(radius != EffectData.DEFAULT_RADIUS && mat == Effect.RECORD_PLAY)
			return discName + (discName.isEmpty() ? "" : "/") + radius;
		if(mat == Material.JUKEBOX || mat == Effect.RECORD_PLAY)
			return discName;
		return "";
	}
	
	@Override
	public void setOn(BlockState state) {
		if(!(state instanceof Jukebox)) {
			OtherDrops.logWarning("Tried to change a jukebox, but no jukebox was found!");
			return;
		}
		((Jukebox)state).setPlaying(disc);
	}
	
	@Override // Jukeboxes are not entities, so nothing to do here
	public void setOn(Entity entity, Player witness) {}

	public static RecordData parse(String state) {
		if(state == null || state.isEmpty()) return null;
		Material mat = Material.getMaterial(state);
		if(mat == null || mat.getId() < 2256) {
			return new RecordData((Material)null);
		}
		return new RecordData(mat);
	}
	
}
