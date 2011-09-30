package com.gmail.zariust.otherdrops.drop;

import static java.lang.Math.round;

import java.util.Random;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

import org.bukkit.Location;

public class RealMoneyDrop extends MoneyDrop {
	public RealMoneyDrop(IntRange money) {
		this(money, 100.0);
	}

	public RealMoneyDrop(IntRange money, boolean shouldSteal) {
		this(money, 100.0, shouldSteal);
	}

	public RealMoneyDrop(IntRange money, double chance) {
		this(money, chance, false);
	}

	public RealMoneyDrop(IntRange money, double percent, boolean shouldSteal) { // Rome
		super(money.toDoubleRange(), percent, shouldSteal);
	}
	
	@Override
	protected int calculateQuantity(double amount, Random rng) {
		total = loot.getRandomIn(rng);
		total = round(total);
		return (int)amount;
	}
	
	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(OtherDrops.moneyDropHandler == null)
			OtherDrops.logWarning("Real money drop has been configured but MoneyDrop is not installed.");
		super.performDrop(source, where, flags);
	}
	
	@Override
	protected void dropMoney(Target source, Location where, DropFlags flags, double amount) {
		if(OtherDrops.moneyDropHandler == null) {
			super.dropMoney(source, where, flags, amount);
			return;
		}
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
}
