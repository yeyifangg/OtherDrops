package com.gmail.zariust.otherdrops.drop;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.register.payment.Method.MethodAccount;

public class MoneyDrop extends DropType {
	private double loot;
	private boolean steal;
	// Without this separate total, the amount dropped would increase every time if there is both
	// an embedded quantity and an external quantity.
	private double total;
	private int realDrop;
	
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
		this(money, percent, shouldSteal, 0);
	}

	public MoneyDrop(double money, double percent, boolean shouldSteal, int real) { // Rome
		super(DropCategory.MONEY, percent);
		loot = money;
		steal = shouldSteal;
		realDrop = real;
		// Round the money to the nearest x decimal places as specified in the global config
		double factor = pow(10, OtherDrops.plugin.config.moneyPrecision);
		loot *= factor;
		loot = round(loot);
		loot /= factor;
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
		if(realDrop > 0) {
			total = loot;
			return (int)amount;
		}
		total = loot * amount;
		return 1;
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if (flags.recipient == null) return;
		Player victim = null;
		if(source instanceof PlayerSubject) victim = ((PlayerSubject)source).getPlayer();
		if (OtherDrops.method == null) {
			OtherDrops.logWarning("Money drop has been configured but no economy plugin has been detected.");
			return;
		}
		
		if (realDrop > 0) {
			int bundles = realDrop;
			while(bundles-- > 0) OtherDrops.moneyDropHandler.dropMoney(where, (int)total);			
		} else {
			if (OtherDrops.method.hasAccount(flags.recipient.getName()))
				OtherDrops.method.getAccount(flags.recipient.getName()).add(total);
			if(!steal || victim == null) return;
			if(OtherDrops.method.hasAccount(victim.getName())) {
				// Make sure that the theft doesn't put them into a negative balance
				MethodAccount account = OtherDrops.method.getAccount(victim.getName());
				double balance = account.balance();
				if(balance <= 0) return; // Don't want the theft to increase their balance either.
				account.subtract(min(balance, loot));
			}
		}
	}


	public static DropType parse(String drop, String data, double amount, double chance) {
		String[] split = drop.toUpperCase().split("@");
		boolean steal = drop.equals("MONEY[-_]STEAL");
		int realDrop = drop.matches("MONEY[-_]DROP") ? (amount > 0 ? (int)amount : 1) : 0;
		if(split.length > 1) data = split[1];
		double numData = 0;
		try {
			numData = Double.parseDouble(data);
		} catch(NumberFormatException e) {}
		if(numData == 0) return new MoneyDrop(amount, chance, steal, realDrop);
		else if(realDrop > 0) return new MoneyDrop(numData, chance, steal, realDrop);
		return new MoneyDrop(numData / amount, chance, steal, realDrop);
		//FIXME: money drops allowing random money drops?
	}

	@Override
	public String toString() {
		return "MONEY@" + loot;
	}
}
