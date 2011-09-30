package com.gmail.zariust.otherdrops.drop;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.register.payment.Method.MethodAccount;

public class MoneyDrop extends DropType {
	protected DoubleRange loot;
	protected boolean steal;
	// Without this separate total, the amount dropped would increase every time if there is both
	// an embedded quantity and an external quantity.
	protected double total;
	
	public MoneyDrop(DoubleRange money) {
		this(money, 100.0);
	}
	
	public MoneyDrop(DoubleRange money, boolean shouldSteal) {
		this(money, 100.0, shouldSteal);
	}
	
	public MoneyDrop(DoubleRange money, double chance) {
		this(money, chance, false);
	}

	public MoneyDrop(DoubleRange money, double percent, boolean shouldSteal) { // Rome
		super(DropCategory.MONEY, percent);
		loot = money;
		steal = shouldSteal;
	}

	@Override
	public double getAmount() {
		// Round the money to the nearest x decimal places as specified in the global config
		return total;
	}
	
	@Override
	public boolean isQuantityInteger() {
		return false;
	}

	@Override
	protected int calculateQuantity(double amount, Random rng) {
		total = loot.getRandomIn(rng);
		total *= amount;
		double factor = pow(10, OtherDrops.plugin.config.moneyPrecision);
		total *= factor;
		total = round(total);
		total /= factor;
		return 1;
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(!canDrop(flags)) return;
		Player victim = null;
		if(source instanceof PlayerSubject) victim = ((PlayerSubject)source).getPlayer();
		if(!steal || victim == null) return;
		double amount = total;
		if(OtherDrops.method.hasAccount(victim.getName())) {
			// Make sure that the theft doesn't put them into a negative balance
			MethodAccount account = OtherDrops.method.getAccount(victim.getName());
			double balance = account.balance();
			if(balance <= 0) return; // Don't want the theft to increase their balance either.
			amount = min(balance, amount);
			account.subtract(amount);
		}
		dropMoney(source, where, flags, amount);
	}
	
	@SuppressWarnings("unused")
	protected void dropMoney(Target source, Location where, DropFlags flags, double amount) {
		if (OtherDrops.method.hasAccount(flags.recipient.getName()))
			OtherDrops.method.getAccount(flags.recipient.getName()).add(amount);
	}

	private boolean canDrop(DropFlags flags) {
		if (flags.recipient == null) return false;
		if (OtherDrops.method == null) {
			OtherDrops.logWarning("Money drop has been configured but no economy plugin has been detected.");
			return false;
		}
		return true;
	}

	public static DropType parse(String drop, String data, DoubleRange amount, double chance) {
		//if(drop.toUpperCase().contains("DROP")) return RealMoneyDrop.parse(drop, data, amount.toIntRange(), chance);
		String[] split = drop.toUpperCase().split("@");
		boolean real = split[0].matches("MONEY[_- ]DROP");
		if(!real && !split[0].equals("MONEY")) return null; // Invalid type of money
		if(split.length > 1) data = split[1];
		boolean steal = data.equals("STEAL");
		if(!steal && !data.isEmpty())
			OtherDrops.logWarning("Invalid data for " + split[0] + ": " + data);
		if(real)
			return new RealMoneyDrop(amount.toIntRange(), chance, steal);
		else return new MoneyDrop(amount, chance, steal);
	}

	@Override
	public String getName() {
		return "MONEY";
	}

	@Override
	public DoubleRange getAmountRange() {
		return loot;
	}
}
