package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.subject.Target;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;

public class ExperienceDrop extends DropType {
	private int total;

	public ExperienceDrop(int amount, double chance) {
		super(DropCategory.EXPERIENCE, chance);
		total = amount;
	}

	@Override
	protected void performDrop(Target source, Location from, DropFlags flags) {
		if(flags.spread) {
			int amount = total, digit = 10;
			while(amount > 0) {
				int inThis = amount % digit;
				amount -= inThis;
				digit *= 10;
				if(inThis > 0) {
					ExperienceOrb orb = from.getWorld().spawn(from, ExperienceOrb.class);
					orb.setExperience(inThis);
				}
			}
		} else {
			ExperienceOrb orb = from.getWorld().spawn(from, ExperienceOrb.class);
			orb.setExperience(total);
		}
	}
	
	@Override
	public double getAmount() {
		return total;
	}
	
	@Override
	public String toString() {
		return "XP@" + total;
	}

	public static DropType parse(String drop, String data, int amount, double chance) {
		String[] split = drop.toUpperCase().split("@");
		if(split.length > 1) data = split[1];
		int numData = 0;
		try {
			numData = Integer.parseInt(data);
		} catch(NumberFormatException e) {}
		if(numData == 0) return new ExperienceDrop(amount, chance);
		return new ExperienceDrop(numData / amount, chance);
		//FIXME: xp drops allowing random xp drops?
	}
	
}
