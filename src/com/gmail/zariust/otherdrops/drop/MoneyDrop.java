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
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;

public class MoneyDrop extends DropType {
	protected DoubleRange loot;
	protected boolean steal;
	protected boolean penalty;
	
	public MoneyDrop(DoubleRange money) {
		this(money, 100.0);
	}
	
	public MoneyDrop(DoubleRange money, boolean shouldSteal) {
		this(money, 100.0, shouldSteal);
	}
	
	public MoneyDrop(DoubleRange money, double chance) {
		this(money, chance, false);
	}

	public MoneyDrop(DoubleRange money, double chance, boolean shouldSteal) {
		this(money, chance, shouldSteal, false);
	}

	public MoneyDrop(DoubleRange amount, double chance, boolean shouldSteal, boolean penalty) { // Rome
		super(DropCategory.MONEY, chance);
		loot = amount;
		steal = shouldSteal;
		this.penalty = penalty;
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
	protected int performDrop(Target source, Location where, DropFlags flags, OccurredEvent occurrence) {
		occurrence.setOverrideDefault(this.overrideDefault);
		
		Player victim = null;
		double amount = total;

		if(source instanceof PlayerSubject) victim = ((PlayerSubject)source).getPlayer();
		if (victim != null) {
			if(steal && Dependencies.hasVaultEcon()) {
				Log.logInfo("(vault)Stealing money ("+amount+") from "+victim.getName()+", giving to "+(flags.recipient == null ? "no-one" : flags.recipient.getName())+".", Verbosity.HIGHEST);				
				double balance = Dependencies.getVaultEcon().getBalance(victim.getName());
				if(balance <= 0) return 0;
				amount = min(balance,amount);
				Dependencies.getVaultEcon().withdrawPlayer(victim.getName(), amount);
			}
		} else {
			Log.logInfo("Giving money ("+amount+") to "+(flags.recipient == null ? "no-one" : flags.recipient.getName())+"", Verbosity.HIGHEST);
		}
		if(!canDrop(flags)) {
			return 0;
		}
		
		if(penalty && Dependencies.hasVaultEcon()) {
			Log.logInfo("(vault)Reducing attacker ("+flags.recipient.getName()+"funds by ("+amount+")", Verbosity.HIGHEST);
			double balance = Dependencies.getVaultEcon().getBalance(flags.recipient.getName());
			Dependencies.getVaultEcon().withdrawPlayer(flags.recipient.getName(), amount);
			return 1;
		}
			
		dropMoney(source, where, flags, amount);
		return 1;
	}
	
	@SuppressWarnings("unused")
	protected void dropMoney(Target source, Location where, DropFlags flags, double amount) {
		if (Dependencies.hasVaultEcon()) {
			Dependencies.getVaultEcon().depositPlayer(flags.recipient.getName(), amount); // TODO: is this right?  Or check for accounts still?
			Log.logInfo("Funds deposited via VAULT.", Verbosity.HIGHEST);
		}
	}

	private boolean canDrop(DropFlags flags) {
		if (flags.recipient == null) {
			Log.logInfo("MoneyDrop - recipient is null, cannot give money to recipient.", Verbosity.HIGH);
			return false;
		}
		if (!Dependencies.hasVaultEcon()) {
			Log.logWarning("Money drop has been configured but no economy plugin has been detected.");
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
		boolean penalty = data.equals("PENALTY");
		if(!steal && !data.isEmpty() && !data.equals("0"))
			Log.logWarning("Invalid data for " + split[0] + ": " + data);
		if(real)
			return new RealMoneyDrop(amount.toIntRange(), chance, steal); // TODO: should reduce apply to moneydrop?
		else return new MoneyDrop(amount, chance, steal, penalty);
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
