// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
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

package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.gmail.zariust.bukkit.otherblocks.PlayerWrapper;
import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.options.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.DropType;
import com.gmail.zariust.bukkit.otherblocks.options.Range;
import com.gmail.zariust.bukkit.otherblocks.options.Target;

public class SimpleDrop extends CustomDrop
{
	// Actions
	private DropType dropped;
	private Range<Double> quantity;
	private Range<Integer> attackerDamage;
	private Range<Short> toolDamage;
	private double dropSpread;
	private MaterialData replacementBlock;
	private List<DropEvent> events;
	private List<String> commands;
	private List<String> messages;
	private Set<Effect> effects;
	
	// Constructors TODO: Expand!?
	public SimpleDrop(Target targ, Action act) {
		super(targ, act);
	}
	
	// Tool Damage
	public short getRandomToolDamage()
	{
		if (toolDamage.getMin() == toolDamage.getMax()) return toolDamage.getMin();
		
		short randomVal = (short) (toolDamage.getMin() + rng.nextInt(toolDamage.getMax() - toolDamage.getMin() + 1));
		return randomVal;
	}
	
	public String getToolDamageRange() {
		return toolDamage.getMin().equals(toolDamage.getMax()) ? toolDamage.getMin().toString() : toolDamage.getMin().toString() + "-" + toolDamage.getMax().toString();
	}

	public void setToolDamage(short val) {
		toolDamage = new Range<Short>(val, val);
	}
	
	public void setToolDamage(short low, short high) {
		toolDamage = new Range<Short>(low, high);
	}
	
	// Quantity getters and setters


	public int getRandomQuantityInt() {
		double random = getRandomQuantityDouble();
		int intPart = (int) random;
		// .intValue() discards the decimal place - round up if neccessary
		if (random - intPart >= 0.5) {
				intPart = intPart + 1;
		}
		return intPart;
	}

	public double getRandomQuantityDouble() {
		//TODO: fix this function so we don't need to multiply by 100
		// this will cause an error if the number is almost max float
		// but a drop that high would crash the server anyway
		double min = (quantity.getMin() * 100);
		double max = (quantity.getMax() * 100);
		int val = (int)min + rng.nextInt((int)max - (int)min + 1);
		double doubleVal = Double.valueOf(val); 
		double deciVal = doubleVal/100;
		return deciVal;
	}
	
	public String getQuantityRange() {
		return quantity.getMin().equals(quantity.getMax()) ? quantity.getMin().toString() : quantity.getMin().toString() + "-" + quantity.getMax().toString();
	}
	
	public void setQuantity(double val) {
		quantity = new Range<Double>(val, val);
	}
	
	public void setQuantity(double low, double high) {
		quantity = new Range<Double>(low, high);
	}

	// The drop
	public void setDropped(DropType drop) {
		this.dropped = drop;
	}

	public DropType getDropped() {
		return dropped;
	}

	// The drop spread chance
	public void setDropSpread(double spread) {
		this.dropSpread = spread;
	}
	
	public void setDropSpread(boolean spread) {
		this.dropSpread = spread ? 100.0 : 0.0;
	}

	public double getDropSpreadChance() {
		return dropSpread;
	}

	public boolean getDropSpread() {
		if(dropSpread >= 100.0) return true;
		else if(dropSpread <= 0.0) return false;
		return rng.nextDouble() > dropSpread / 100.0;
	}

	// Attacker Damage
	public int getRandomAttackerDamage()
	{
		if (attackerDamage.getMin() == attackerDamage.getMax()) return attackerDamage.getMin();
		
		int randomVal = (attackerDamage.getMin() + rng.nextInt(attackerDamage.getMax() - attackerDamage.getMin() + 1));
		return randomVal;
	}
	
	public String getAttackerDamageRange() {
		return attackerDamage.getMin().equals(attackerDamage.getMax()) ? attackerDamage.getMin().toString() : attackerDamage.getMin().toString() + "-" + attackerDamage.getMax().toString();
	}

	public void setAttackerDamage(int val) {
		attackerDamage = new Range<Integer>(val, val);
	}
	
	public void setAttackerDamage(int low, int high) {
		attackerDamage = new Range<Integer>(low, high);
	}
	
	// Replacement
	public MaterialData getReplacement() {
		return replacementBlock;
	}
	
	public void setReplacement(MaterialData block) {
		if(!block.getItemType().isBlock()) throw new IllegalArgumentException("replacementblock must be a block");
		replacementBlock = block;
	}

	// Events
	public void setEvents(List<DropEvent> evt) {
		this.events = evt;
	}

	public List<DropEvent> getEvents() {
		return events;
	}

	// Commands
	public void setCommands(List<String> cmd) {
		this.commands = cmd;
	}

	public List<String> getCommands() {
		return commands;
	}

	// Messages
	public void setMessages(List<String> msg) {
		this.messages = msg;
	}

	public String getRandomMessage(double amount) {
		String msg = messages.get(rng.nextInt(messages.size()));
		msg = msg.replace("%q", Double.toString(amount));
		// TODO: Colour codes
		return msg;
	}

	public String getRandomMessage(int amount) {
		if(messages == null || messages.isEmpty()) return null;
		String msg = messages.get(rng.nextInt(messages.size()));
		msg = msg.replace("%q", Integer.toString(amount));
		// TODO: Colour codes
		return msg;
	}

	// Effects
	public void setEffects(Set<Effect> sfx) {
		this.effects = sfx;
	}

	public Set<Effect> getEffects() {
		return effects;
	}

	@Override
	public void run() {
		// We need a player for some things.
		Player who = null;
		if(event.getAgent() instanceof Player) who = (Player) event.getAgent();
		// We also need the location
		Location location = event.getLocation();
		// Effects first
		for(Effect effect : effects) {
			// TODO: Data, radius
			location.getWorld().playEffect(location, effect, 0);
		}
		// Now events TODO
		// Then the actual drop; if it's deny, the event is cancelled
		// Note that deny WILL NOT WORK with delay; if you try to do that,
		// the default drop will most likely drop. In fact, delay along with drop in general
		// may have unexpected effects.
		boolean dropNaturally = true; // TODO: How to make this specifiable in the config?
		boolean spreadDrop = getDropSpread();
		double amount = getRandomQuantityDouble();
		dropped.drop(location, amount, who, dropNaturally, spreadDrop);
		// Send a message, if any
		if(who != null) {
			String msg = getRandomMessage(amount);
			if(msg != null) who.sendMessage(msg);
		}
		// Run commands, if any
		if(commands != null) {
			CommandSender from;
			ConsoleCommandSender console = new ConsoleCommandSender(Bukkit.getServer());
			if(who != null) from = new PlayerWrapper(who);
			else from = console;
			for(String command : commands) {
				if(who != null) command = command.replaceAll("%p", who.getName());
				if(command.startsWith("/")) command = command.substring(1);
				if(command.startsWith("!")) command = command.substring(1);
				else from = console;
				if(command.startsWith("*")) command = command.substring(1);
				else if(who != null) from = who;
				Bukkit.getServer().dispatchCommand(from, command);
			}
		}
		// Replacement block
		Target target = event.getTarget();
		// Tool damage
		// Attacker damage
	}
}
