// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;

public class ExperienceDrop extends DropType {
	private IntRange total;
	private int rolledXP;

	public ExperienceDrop(IntRange amount, double chance) {
		super(DropCategory.EXPERIENCE, chance);
		total = amount;
	}

	@Override
	protected void performDrop(Target source, Location from, DropFlags flags) {
		rolledXP = total.getRandomIn(flags.rng);
		if(flags.spread) {
			int amount = rolledXP, digit = 10;
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
			orb.setExperience(rolledXP);
		}
	}
	
	@Override
	public double getAmount() {
		return rolledXP;
	}
	
	@Override
	public String getName() {
		return "XP";
	}

	public static DropType parse(String drop, String data, IntRange amount, double chance) {
		String[] split = drop.toUpperCase().split("@");
		if(split.length > 1) data = split[1];
		int numData = 0;
		try {
			numData = Integer.parseInt(data);
		} catch(NumberFormatException e) {}
		if(numData == 0) return new ExperienceDrop(amount, chance);
		if(amount.getMax().equals(amount.getMax())) {
			amount.setMax(numData / amount.getMax());
			amount.setMin(amount.getMax());
		} else {
			amount.setMax(numData / amount.getMin());
		}
		return new ExperienceDrop(amount, chance);
		//FIXME: xp drops allowing random xp drops?
	}

	@Override
	public DoubleRange getAmountRange() {
		return total.toDoubleRange();
	}
	
}
