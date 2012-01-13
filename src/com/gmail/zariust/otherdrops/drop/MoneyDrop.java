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

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.register.payment.Method.MethodAccount;

public class MoneyDrop extends DropType {
	protected DoubleRange loot;
	protected boolean steal;
	
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
		// Round the money to the nearest x decimal places as specified in the global config
		double factor = pow(10, OtherDrops.plugin.config.moneyPrecision);
		total *= factor;
		total = round(total);
		total /= factor;
		return 1;
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(!canDrop(flags)) {
			OtherDrops.logInfo("Checked flags - cannot drop...", Verbosity.HIGH);
			return;
		}
		Player victim = null;
		if(source instanceof PlayerSubject) victim = ((PlayerSubject)source).getPlayer();
		double amount = total;
		OtherDrops.logInfo("Dropping money - "+amount+".", Verbosity.HIGHEST);
		if(steal && OtherDrops.method.hasAccount(victim.getName())) {
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
		String[] split = drop.toUpperCase().split("@");
		boolean real = split[0].matches("MONEY[ _-]DROP");
		if(!real && !split[0].equals("MONEY")) return null; // Invalid type of money
		if(split.length > 1) data = split[1];
		boolean steal = data.equals("STEAL");
		if(!steal && !data.isEmpty() && !data.equals("0"))
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
