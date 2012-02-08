package com.gmail.zariust.otherdrops.event;

import static com.gmail.zariust.common.Verbosity.HIGH;
import static com.gmail.zariust.common.Verbosity.HIGHEST;
import static com.gmail.zariust.common.Verbosity.NORMAL;
import static java.lang.Math.max;

import java.awt.Event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.PlayerWrapper;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.options.SoundEffect;
import com.gmail.zariust.otherdrops.options.ToolDamage;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.LivingSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ProjectileAgent;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;

public class DropRunner implements Runnable{
	private OtherDrops plugin;
	OccurredEvent currentEvent;
	SimpleDrop customDrop;
	Player player;
	Location playerLoc;
	boolean defaultDrop;

	public DropRunner(OtherDrops otherblocks, OccurredEvent target, SimpleDrop dropData, Player player, Location playerLoc, boolean defaultDrop) {
		this.plugin = otherblocks;
		this.currentEvent = target;
		this.customDrop = dropData;
		this.player = player;
		this.playerLoc = playerLoc;
		this.defaultDrop = defaultDrop;
	}

	public DropRunner(OtherDrops plugin2, OccurredEvent evt,
			CustomDrop customDrop2, Player player2, Location playerLoc2, boolean defaultDrop) {
		this.plugin = plugin2;
		this.currentEvent = evt;
		if (customDrop2 instanceof SimpleDrop)
			this.customDrop = (SimpleDrop)customDrop2;
		else
			OtherDrops.logWarning("DropRunner: customdrop is not simple. Customdrop: "+customDrop2.toString(), Verbosity.NORMAL);
		this.player = player2;
		this.playerLoc = playerLoc2;
		this.defaultDrop = defaultDrop;
	}

	//@Override
	public void run() {
		OtherDrops.logInfo("Starting SimpleDrop...",Verbosity.EXTREME);
		ProfilerEntry entry = new ProfilerEntry("DROP");
		OtherDrops.profiler.startProfiling(entry);
		// We need a player for some things.
		Player who = null;
		if(currentEvent.getTool() instanceof PlayerSubject) who = ((PlayerSubject) currentEvent.getTool()).getPlayer();
		if(currentEvent.getTool() instanceof ProjectileAgent) {
			LivingSubject living = ((ProjectileAgent) currentEvent.getTool()).getShooter();
			OtherDrops.logInfo("droprunner.run: proejctile agent detected... shooter = "+living.toString(),HIGHEST);			
			if (living instanceof PlayerSubject) who = ((PlayerSubject)living).getPlayer();
		}
		// We also need the location
		Location location = currentEvent.getLocation();
		// Then the actual drop
		// May have unexpected effects when use with delay.
		double amount = 1;
		if (customDrop.getDropped() != null) {
			if(!customDrop.getDropped().toString().equalsIgnoreCase("DEFAULT")) {
				Target target = currentEvent.getTarget();
				boolean dropNaturally = true; // TODO: How to make this specifiable in the config?
				boolean spreadDrop = customDrop.getDropSpread();
				amount = customDrop.quantity.getRandomIn(customDrop.rng);
				DropFlags flags = DropType.flags(who, dropNaturally, spreadDrop, customDrop.rng);
				int droppedQuantity = customDrop.getDropped().drop(target, customDrop.getOffset(), amount, flags);
				OtherDrops.logInfo("SimpleDrop: dropped "+customDrop.getDropped().toString()+" x "+amount+" (dropped: "+droppedQuantity+")",HIGHEST);
				if(droppedQuantity < 0) { // If the embedded chance roll fails, assume default and bail out!
					OtherDrops.logInfo("Drop failed... setting cancelled to false", Verbosity.HIGHEST);
					currentEvent.setCancelled(false); 
					// Profiling info
					OtherDrops.profiler.stopProfiling(entry);
					return;
				}
				// If the drop chance was 100% and no replacement block is specified, make it air
				double chance = max(customDrop.getChance(), customDrop.getDropped().getChance());
				if(customDrop.getReplacementBlock() == null && chance >= 100.0 && target.overrideOn100Percent()) {
					if (target instanceof LivingSubject) {  // need to be careful not to replace creatures with air - this kills the death animation
						currentEvent.setCancelled(true);
					} else if (target instanceof VehicleTarget) {
						currentEvent.setCancelled(true);
						((VehicleTarget) target).getVehicle().remove();
					} else if (currentEvent.getAction() == Action.BREAK) {
						if (!defaultDrop) customDrop.setReplacementBlock(new BlockTarget(Material.AIR));
						currentEvent.setCancelled(false); 
					}
				}
				amount *= customDrop.getDropped().getAmount();
			} else {
				// DEFAULT event - set cancelled to false
				OtherDrops.logInfo("Performdrop: DEFAULT, so undo event cancellation.", Verbosity.HIGHEST);
				currentEvent.setCancelled(false); 
				// TODO: some way of setting it so that if we've set false here we don't set true on the same occureddrop?
				// this could save us from checking the DEFAULT drop outside the loop in OtherDrops.performDrop()
			}
		}
		// Send a message, if any
		if(who != null) {
			if (customDrop.getDropped() instanceof com.gmail.zariust.otherdrops.drop.MoneyDrop) {
				amount = customDrop.getDropped().total;
			}
			String msg = getRandomMessage(customDrop, currentEvent, amount);
			if(msg != null) who.sendMessage(msg);
		} else {
			OtherDrops.logInfo("Performdrop: 'who' is null so not sending any message.", Verbosity.EXTREME);
		}
		// Run commands, if any
		if(customDrop.getCommands() != null) {
			for(String command : customDrop.getCommands()) {
				boolean suppress = false;
				Boolean override = false;
				// Five possible prefixes (slash is optional in all of them)
				//   "/" - Run the command as the player, and send them any result messages
				//  "/!" - Run the command as the player, but send result messages to the console
				//  "/*" - Run the command as the player with op override, and send them any result messages
				// "/!*" - Run the command as the player with op override, but send result messages to the console
				//  "/$" - Run the command as the console, but send the player any result messages
				// "/!$" - Run the command as the console, but send result messages to the console
				if(who != null) command = command.replaceAll("%p", who.getName());
				if(command.startsWith("/")) command = command.substring(1);
				if(command.startsWith("!")) {
					command = command.substring(1);
					suppress = true;
				}
				if(command.startsWith("*")) {
					command = command.substring(1);
					override = true;
				} else if(command.startsWith("$")) {
					command = command.substring(1);
					override = null;
				}
				CommandSender from;
				if(who == null || override == null) from = Bukkit.getConsoleSender();
				else from = new PlayerWrapper(who, override, suppress);
				Bukkit.getServer().dispatchCommand(from, command);
			}
		}
		// Replacement block
		if(customDrop.getReplacementBlock() != null) {  // note: we shouldn't change the replacementBlock, just a copy of it.
			Target toReplace = currentEvent.getTarget();
			BlockTarget tempReplace = customDrop.getReplacementBlock();
			if(customDrop.getReplacementBlock().getMaterial() == null) {
				tempReplace = new BlockTarget(toReplace.getLocation().getBlock());
			}
			OtherDrops.logInfo("Replacing "+toReplace.toString() + " with "+customDrop.getReplacementBlock().toString(), Verbosity.HIGHEST);
			toReplace.setTo(tempReplace);
			currentEvent.setCancelled(true);
		}
		
		// Effects after replacement block
		// TODO: I don't think effect should account for randomize/offset.
		if (customDrop.getEffects() != null) for(SoundEffect effect : customDrop.getEffects()) 
			effect.play(customDrop.randomiseLocation(location, customDrop.randomize));

		Agent used = currentEvent.getTool();
		if (used != null) {  // there's no tool for leaf decay
			// Tool damage
			if(customDrop.getToolDamage() != null) {
				used.damageTool(customDrop.getToolDamage(), customDrop.rng);
			} else {
				if (currentEvent.getEvent() instanceof BlockBreakEvent)
					used.damageTool(new ToolDamage(1), customDrop.rng);				
			}

			// Attacker damage
			if(customDrop.getAttackerDamage() != null) {
				int damage = customDrop.getAttackerDamage().getRandomIn(customDrop.rng);
				used.damage(damage);
			}
		}
		try {
			Location oldLocation = currentEvent.getLocation();
			customDrop.randomiseLocation(currentEvent.getLocation(), customDrop.randomize);
			// And finally, events
			if (customDrop.getEvents() != null) {
				for(SpecialResult evt : customDrop.getEvents()) {
					if(evt.canRunFor(currentEvent)) evt.executeAt(currentEvent);
				}
			}
			currentEvent.setLocation(oldLocation);
			currentEvent = null; // Zar: testing as we should not need current event anymore
		} catch (Exception ex) {
			OtherDrops.logWarning("Exception while running special event results: " + ex.getMessage(), NORMAL);
			if(OtherDrops.plugin.config.getVerbosity().exceeds(HIGH)) ex.printStackTrace();
		}
		// Profiling info
		OtherDrops.profiler.stopProfiling(entry);
	}

	static public String getRandomMessage(CustomDrop drop, OccurredEvent occurence, double amount) {
		if(drop.getMessages() == null || drop.getMessages().isEmpty()) return null;
		String msg = drop.getMessages().get(drop.rng.nextInt(drop.getMessages().size()));
		msg = msg.replace("%Q", "%q");
		if(drop instanceof SimpleDrop) {
			if (((SimpleDrop)drop).getDropped() != null) {
				if(((SimpleDrop)drop).getDropped().isQuantityInteger())
					msg = msg.replace("%q", String.valueOf(Math.round(amount)));
				else msg = msg.replace("%q", Double.toString(amount));
			}
		}
		msg = msg.replace("%d", drop.getDropName().toLowerCase());
		msg = msg.replace("%D", drop.getDropName().toUpperCase());
		// TODO: this doesn't work - just returns "PLAYER" rather than the tool they used
		msg = msg.replace("%t", occurence.getTool().toString().toLowerCase());
		msg = msg.replace("%T", occurence.getTool().toString().toUpperCase());
		msg = msg.replaceAll("&([0-9a-fA-F])", "§$1"); // replace color codes
		msg = msg.replace("&&", "&"); // replace "escaped" ampersand
		return msg;
	}
}