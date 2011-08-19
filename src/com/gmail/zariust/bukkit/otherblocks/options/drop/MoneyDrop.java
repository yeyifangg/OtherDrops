package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
	protected int calculateQuantity(double amount) {
		loot *= amount;
		return 1;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		OtherBlocks.method.getAccount(flags.recipient.getName()).add(loot);
	}
}
