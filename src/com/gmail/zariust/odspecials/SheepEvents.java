package com.gmail.zariust.odspecials;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.CreatureType;

import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.special.SpecialResultHandler;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.Target;

public class SheepEvents extends SpecialResultHandler {
	@Override
	public SpecialResult getNewEvent(String name) {
		if(name.equalsIgnoreCase("SHEAR")) return new ShearEvent(this, true);
		else if(name.equalsIgnoreCase("UNSHEAR")) return new ShearEvent(this, false);
		else if(name.equalsIgnoreCase("SHEARTOGGLE")) return new ShearEvent(this, null);
		else if(name.equalsIgnoreCase("DYE")) return new DyeEvent(this);
		return null;
	}
	
	@Override
	public void onLoad() {
		logInfo("Sheep v" + getVersion() + " loaded.");
	}
	
	@Override
	public List<String> getEvents() {
		return Arrays.asList("SHEAR", "UNSHEAR", "SHEARTOGGLE", "DYE");
	}
	
	@Override
	public String getName() {
		return "Sheep";
	}
	
	public static boolean canRunFor(AbstractDropEvent drop) {
		Target target = drop.getTarget();
		if(!(target instanceof CreatureSubject)) return false;
		CreatureSubject creature = (CreatureSubject) target;
		if(creature.getCreature() != CreatureType.SHEEP) return false;
		if(creature.getAgent() != null && creature.getAgent().isDead()) return false;
		return true;
	}
	
}
