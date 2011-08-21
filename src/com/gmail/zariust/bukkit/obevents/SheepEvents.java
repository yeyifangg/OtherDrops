package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.CreatureType;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.event.DropEventHandler;
import com.gmail.zariust.bukkit.otherblocks.subject.CreatureSubject;
import com.gmail.zariust.bukkit.otherblocks.subject.Target;

public class SheepEvents extends DropEventHandler {
	private OtherBlocks otherblocks;
	
	public SheepEvents(OtherBlocks plugin) {
		otherblocks = plugin;
	}
	
	@Override
	public DropEvent getNewEvent(String name) {
		if(name.equalsIgnoreCase("SHEAR")) return new ShearEvent(this, true);
		else if(name.equalsIgnoreCase("UNSHEAR")) return new ShearEvent(this, false);
		else if(name.equalsIgnoreCase("SHEARTOGGLE")) return new ShearEvent(this, null);
		else if(name.equalsIgnoreCase("DYE")) return new DyeEvent(this);
		return null;
	}
	
	@Override
	public void onLoad() {
		setVersion(info.getProperty("version"));
		logInfo("Trees v" + getVersion() + " loaded.");
	}
	
	@Override
	public List<String> getEvents() {
		return Arrays.asList("SHEAR", "UNSHEAR", "SHEARTOGGLE", "DYE");
	}
	
	@Override
	public String getName() {
		return "Sheep";
	}
	
	public static boolean canRunFor(AbstractDrop drop) {
		Target target = drop.getTarget();
		if(!(target instanceof CreatureSubject)) return false;
		CreatureSubject creature = (CreatureSubject) target;
		if(creature.getCreature() != CreatureType.SHEEP) return false;
		return true;
	}
	
}
