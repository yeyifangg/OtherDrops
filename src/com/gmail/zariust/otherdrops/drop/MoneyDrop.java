package com.gmail.zariust.otherdrops.drop;

import static java.lang.Math.min;

import org.bukkit.Location;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.register.payment.Method.MethodAccount;

public class MoneyDrop extends DropType {
	private double loot;
	private boolean steal;
	// Without this separate total, the amount dropped would increase every time if there is both
	// an embedded quantity and an external quantity.
	private double total;
	
	public MoneyDrop(double money) {
		this(money, 100.0);
	}
	
	public MoneyDrop(double money, boolean shouldSteal) {
		this(money, 100.0, shouldSteal);
	}
	
	public MoneyDrop(double money, double chance) {
		this(money, chance, false);
	}
	
	public MoneyDrop(double money, double percent, boolean shouldSteal) {
		super(DropCategory.MONEY, percent);
		loot = money;
		steal = shouldSteal;
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
		if(!steal || flags.victim == null) return;
		if(OtherDrops.method.hasAccount(flags.victim.getName())) {
			// Make sure that the theft doesn't put them into a negative balance
			MethodAccount account = OtherDrops.method.getAccount(flags.victim.getName());
			double balance = account.balance();
			if(balance <= 0) return; // Don't want the theft to increase their balance either.
			account.subtract(min(balance, loot));
		}
	}

	public static DropType parse(String drop, String data, double amount, double chance) {
		String[] split = drop.toUpperCase().split("@");
		boolean steal = drop.equals("MONEY_STEAL");
		if(split.length > 1) data = split[1];
		double numData = 0;
		try {
			numData = Double.parseDouble(data);
		} catch(NumberFormatException e) {}
		if(numData == 0) return new MoneyDrop(amount, chance, steal);
		return new MoneyDrop(numData / amount, chance, steal);
	}

	@Override
	public String toString() {
		return "MONEY@" + loot;
	}
}
