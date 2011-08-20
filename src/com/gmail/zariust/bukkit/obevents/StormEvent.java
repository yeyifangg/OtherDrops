package com.gmail.zariust.bukkit.obevents;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;

public class StormEvent extends DropEvent {
	private int duration = -1;
	
	public StormEvent(WeatherEvents source) {
		super("STORM", source);
	}

	@Override
	public void executeAt(OccurredDrop event) {
		World world = event.getWorld();
		if(duration == 0) world.setStorm(false);
		else {
			world.setStorm(true);
			if(duration > 0) world.setWeatherDuration(duration);
		}
	}
	
	@Override
	public void interpretArguments(String... args) {
		if(args.length > 0) {
			String time = args[0];
			if(time.equalsIgnoreCase("ON")) {
				duration = -1;
				used(args[0]);
			} else if(time.equalsIgnoreCase("OFF")) {
				duration = 0;
				used(args[0]);
			} else try {
				duration = Integer.parseInt(time);
				used(args[0]);
			} catch(NumberFormatException e) {}
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		Map<Biome, Boolean> biomes = drop.getBiome();
		// By using Boolean.TRUE I eliminate the need to check for null
		if(biomes.get(Biome.HELL) == Boolean.TRUE) return false;
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		Biome biome = drop.getBiome();
		if(biome == Biome.HELL) return false;
		return true;
	}
	
}
