package com.gmail.zariust.bukkit.otherblocks.droptype;

import org.bukkit.Location;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public class MoneyDrop extends DropType {
	private double loot;
	
	public MoneyDrop(double money) {
		this(money, 100.0);
	}
	
	public MoneyDrop(double money, double percent) {
		super(DropCategory.MONEY, percent);
		loot = money;
	}

	public double getMoney() {
		return loot;
	}
	
	@Override
	public boolean isQuantityInteger() {
		return false;
	}

	@Override
	protected int calculateQuantity(double amount) {
		loot *= amount;
		return 1;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		if (flags.recipient == null) return;
		if (OtherBlocks.method.hasAccount(flags.recipient.getName()))
			OtherBlocks.method.getAccount(flags.recipient.getName()).add(loot);
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
