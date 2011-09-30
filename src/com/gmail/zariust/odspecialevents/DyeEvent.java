package com.gmail.zariust.odspecialevents;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Sheep;

import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ToolAgent;

public class DyeEvent extends SpecialResult {
	private DyeColor colour = null;
	
	public DyeEvent(SheepEvents source) {
		super("DYE", source);
	}

	@Override
	public void executeAt(OccurredDropEvent event) {
		DyeColor dye = DyeColor.PINK;
		if(colour == null) {
			Agent agent = event.getTool();
			if(agent instanceof PlayerSubject) {
				ToolAgent tool = ((PlayerSubject) agent).getTool();
				if(tool.getMaterial() == Material.INK_SACK)
					dye = DyeColor.getByData((byte) (0xF - tool.getData().getData()));
			}
			if(colour == null) dye = DyeColor.getByData((byte) event.getRandom(16));
		} else dye = colour;
		CreatureSubject target = (CreatureSubject) event.getTarget();
		Sheep sheep = (Sheep) target.getAgent();
		sheep.setColor(dye);
	}
	
	@Override
	public void interpretArguments(List<String> args) {
		for(String arg : args) {
			try {
				colour = DyeColor.valueOf(arg);
				used(arg);
			} catch(IllegalArgumentException e) {}
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDropEvent drop) {
		return SheepEvents.canRunFor(drop);
	}
	
	@Override
	public boolean canRunFor(OccurredDropEvent drop) {
		return SheepEvents.canRunFor(drop);
	}
	
}
