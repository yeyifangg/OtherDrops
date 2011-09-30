package com.gmail.zariust.otherdrops.drop;

import java.util.Random;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

import org.bukkit.Location;

public class RealMoneyDrop extends MoneyDrop {
	private IntRange realDrop;
	
	public RealMoneyDrop(IntRange money) {
		this(money, new IntRange(1));
	}

	public RealMoneyDrop(IntRange money, boolean shouldSteal) {
		this(money, new IntRange(1), shouldSteal);
	}

	public RealMoneyDrop(IntRange money, double chance) {
		this(money, new IntRange(1), chance);
	}

	public RealMoneyDrop(IntRange money, double percent, boolean shouldSteal) {
		this(money, new IntRange(1), percent, shouldSteal);
	}
	
	public RealMoneyDrop(IntRange money, IntRange bundles) {
		this(money, bundles, 100.0);
	}

	public RealMoneyDrop(IntRange money, IntRange bundles, boolean shouldSteal) {
		this(money, bundles, 100.0, shouldSteal);
	}

	public RealMoneyDrop(IntRange money, IntRange bundles, double chance) {
		this(money, bundles, chance, false);
	}

	public RealMoneyDrop(IntRange money, IntRange bundles, double percent, boolean shouldSteal) { // Rome
		super(money.toDoubleRange(), percent, shouldSteal);
		realDrop = bundles;
	}
	
	@Override
	protected int calculateQuantity(double amount, Random rng) {
		return (int)(amount * realDrop.getRandomIn(rng));
	}
	
	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(OtherDrops.moneyDropHandler == null) {
			OtherDrops.logWarning("Real money drop has been configured but MoneyDrop is not installed.");
			super.performDrop(source, where, flags);
			return;
		}
		total = loot.getRandomIn(flags.rng).intValue();
		super.performDrop(source, where, flags);
	}
	
	@Override
	protected void dropMoney(Target source, Location where, DropFlags flags, double amount) {
		if(flags.spread) {
			int dropAmount = (int)amount, digit = 10;
			while(dropAmount > 0) {
				int inThis = dropAmount % digit;
				dropAmount -= inThis;
				digit *= 10;
				if(inThis > 0) OtherDrops.moneyDropHandler.dropMoney(where, inThis);
			}
		} else {
			OtherDrops.moneyDropHandler.dropMoney(where, (int)amount);		
		}
	}
	
	public static DropType parse(String drop, String data, IntRange amount, double chance) {
		String[] split = drop.toUpperCase().split("@");
		boolean steal = split[0].contains("STEAL");
		if(split.length > 1) data = split[1];
		IntRange numData = null;
		try {
			numData = IntRange.parse(data);
		} catch(IllegalArgumentException e) {}
		if(numData == null) return new RealMoneyDrop(new IntRange(1), amount, chance, steal);
		return new RealMoneyDrop(numData, amount, chance, steal);
		//FIXME: money drops allowing random money drops?
	}
}
