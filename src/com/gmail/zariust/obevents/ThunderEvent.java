package com.gmail.zariust.obevents;

import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.gmail.zariust.otherdrops.event.OccurredDrop;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.special.DropEvent;

public class ThunderEvent extends DropEvent {
	private short duration;
	
	public ThunderEvent(WeatherEvents source) {
		super("THUNDER", source);
	}

	@Override
	public void executeAt(OccurredDrop event) {
		World world = event.getWorld();
		if(duration == 0) world.setThundering(false);
		else {
			world.setThundering(true);
			if(duration > 0) world.setThunderDuration(duration);
		}
	}
	
	@Override
	public void interpretArguments(List<String> args) {
		for(String time : args) {
			if(time.equalsIgnoreCase("ON")) {
				duration = -1;
				used(time);
			} else if(time.equalsIgnoreCase("OFF")) {
				duration = 0;
				used(time);
			} else try {
				duration = Short.parseShort(time);
				used(time);
			} catch(NumberFormatException e) {}
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		Map<Biome, Boolean> biomes = drop.getBiome();
		// By using Boolean.TRUE I eliminate the need to check for null
		if(biomes.get(Biome.HELL) == Boolean.TRUE) return false;
		if(biomes.get(Biome.SKY) == Boolean.TRUE) return false;
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		Biome biome = drop.getBiome();
		if(biome == Biome.HELL || biome == Biome.SKY) return false;
		return true;
	}
}
