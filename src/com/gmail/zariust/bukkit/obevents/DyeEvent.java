package com.gmail.zariust.bukkit.obevents;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Sheep;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.subject.Agent;
import com.gmail.zariust.bukkit.otherblocks.subject.CreatureSubject;
import com.gmail.zariust.bukkit.otherblocks.subject.PlayerSubject;
import com.gmail.zariust.bukkit.otherblocks.subject.ToolAgent;

public class DyeEvent extends DropEvent {
	private DyeColor colour = null;
	
	public DyeEvent(SheepEvents source) {
		super("DYE", source);
	}

	@Override
	public void executeAt(OccurredDrop event) {
		DyeColor dye = DyeColor.PINK;
		if(colour == null) {
			Agent agent = event.getTool();
			if(agent instanceof PlayerSubject) {
				ToolAgent tool = ((PlayerSubject) agent).getTool();
				if(tool.getMaterial() == Material.INK_SACK)
					dye = DyeColor.getByData((byte) (0xF - tool.getData()));
			}
			if(colour == null) dye = DyeColor.getByData((byte) event.getRandom(16));
		} else dye = colour;
		CreatureSubject target = (CreatureSubject) event.getTarget();
		Sheep sheep = (Sheep) target.getAgent();
		sheep.setColor(dye);
	}
	
	@Override
	public void interpretArguments(String... args) {
		if(args.length == 0) return;
		try {
			colour = DyeColor.valueOf(args[0]);
			used(args[0]);
		} catch(IllegalArgumentException e) {}
	}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		return SheepEvents.canRunFor(drop);
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		return SheepEvents.canRunFor(drop);
	}
	
}
