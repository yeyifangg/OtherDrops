package com.gmail.zariust.otherdrops.drop;

import org.bukkit.Location;

import com.gmail.zariust.otherdrops.OtherDrops;

public class MoneyDrop extends DropType {
	private double loot;
	// Without this separate total, the amount dropped would increase every time if there is both
	// an embedded quantity and an external quantity.
	private double total;
	
	public MoneyDrop(double money) {
		this(money, 100.0);
	}
	
	public MoneyDrop(double money, double percent) {
		super(DropCategory.MONEY, percent);
		loot = money;
	}

	@Override
	public double getAmount() {
		return loot;
	}
	
	@Override
	public boolean isQuantityInteger() {
		return false;
	}

	@Override
	protected int calculateQuantity(double amount) {
		total = loot * amount;
		return 1;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		if (flags.recipient == null) return;
		if (OtherDrops.method.hasAccount(flags.recipient.getName()))
			OtherDrops.method.getAccount(flags.recipient.getName()).add(total);
	}

	public static DropType parse(String drop, String data, double amount, double chance) {
		String[] split = drop.split("@");
		if(split.length > 1) data = split[1];
		double numData = 0;
		try {
			numData = Double.parseDouble(data);
		} catch(NumberFormatException e) {}
		if(numData == 0) return new MoneyDrop(amount, chance);
		return new MoneyDrop(numData / amount, chance);
	}

	@Override
	public String toString() {
		return "MONEY@" + loot;
	}
}
