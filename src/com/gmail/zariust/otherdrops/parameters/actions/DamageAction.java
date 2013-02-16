package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction.MessageType;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;

public class DamageAction extends Action {
	// "potioneffect: "
	// message.player, message.radius@<r>, message.world, message.server
	public enum DamageActionType {
		ATTACKER, VICTIM, RADIUS, WORLD, SERVER, TOOL
	}

	public enum DamageType {
		NORMAL, FIRE, LIGHTNING
	}

	static Map<String, DamageActionType> matches = new HashMap<String, DamageActionType>();
	static {
		matches.put("damage", DamageActionType.ATTACKER);
		matches.put("damageattacker", DamageActionType.ATTACKER);
		matches.put("damage.attacker", DamageActionType.ATTACKER);
		matches.put("damage.victim", DamageActionType.VICTIM);
		matches.put("damage.server", DamageActionType.SERVER);
		matches.put("damage.world", DamageActionType.WORLD);
		matches.put("damage.global", DamageActionType.SERVER);
		matches.put("damage.all", DamageActionType.SERVER);
		matches.put("damage.radius", DamageActionType.RADIUS);

		
		// Can't do tooldamage yet - need a way to damage tools by "1" if a block break
		// event and this condition hasn't run.
		
		//matches.put("damage.tool", DamageActionType.TOOL);
		//matches.put("damagetool", DamageActionType.TOOL);
	}

	protected DamageActionType damageActionType;
	protected double radius = 10;
	private Map<IntRange, DamageType> damages; // this can contain variables, parse at runtime

	public DamageAction(Object object, DamageActionType damageEffectType2) {
		damageActionType = damageEffectType2;
		damages = new HashMap<IntRange, DamageType>();
		
		if (object instanceof List) {
			// TODO: support lists?
			Log.logInfo("DamageAction: lists not yet supported.");
			List<String> stringList = (List<String>)object;
			for (String sub : stringList) {
				parseDamage(sub);
			}
		} else if (object instanceof String) {
			parseDamage((String)object);
		}
	}

	private void parseDamage(String sub) {
		IntRange value = IntRange.parse("0");
		DamageType type = DamageType.NORMAL;
		
		if (sub.matches("(?i)fire.*")) {
			type = DamageType.FIRE;
			String[] split = sub.split("@");
			if (split.length > 1) value = IntRange.parse(split[1]);
			else value = IntRange.parse("60"); // default to 60 ticks (3 seconds)
		} else if (sub.matches("(?i)lightning.*")) {
			type = DamageType.LIGHTNING;
			String[] split = sub.split("@");
			if (split.length > 1) value = IntRange.parse(split[1]);
			// default of 0 (harmless lightning) is ok.
		} else value = IntRange.parse(sub);

		damages.put(value, type);
	}

	@Override
	public boolean act(CustomDrop drop, OccurredEvent occurence) {
		if (damages != null) {
			for (IntRange key : damages.keySet()) {
				processDamage(drop, occurence, key, damages.get(key));
			}
		}

		return false;
	}

	private void processDamage(CustomDrop drop, OccurredEvent occurence, IntRange damageRange, DamageType damageType) {
		switch (damageActionType) {
		case ATTACKER:
			if (occurence.getPlayerAttacker() != null) {
				damage(occurence.getPlayerAttacker(), damageRange, damageType, drop);
			}
			break;
		case VICTIM:
			if (occurence.getPlayerVictim() != null)
				damage(occurence.getPlayerVictim(), damageRange, damageType, drop);
			else if (occurence.getTarget() instanceof CreatureSubject) {
				Entity ent = ((CreatureSubject)occurence.getTarget()).getEntity();
				if (ent instanceof LivingEntity) {
					damage((LivingEntity)ent, damageRange, damageType, drop);
				}
			}

			break;			
		case RADIUS:
			// occurence.getLocation().getRadiusPlayers()? - how do we get players around radius without an entity?
			Location loc = occurence.getLocation();
			for (Player player : loc.getWorld().getPlayers()) {
				if (player.getLocation().getX() > (loc.getX() - radius) ||
						player.getLocation().getX() < (loc.getX() + radius))
					if (player.getLocation().getY() > (loc.getY() - radius) ||
							player.getLocation().getY() < (loc.getY() + radius))
						if (player.getLocation().getZ() > (loc.getZ() - radius) ||
								player.getLocation().getZ() < (loc.getZ() + radius))
							damage(player, damageRange, damageType, drop);
			}

			break;
		case SERVER:
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				damage(player, damageRange, damageType, drop);
			}
			break;
		case WORLD:
			for (Player player : occurence.getLocation().getWorld().getPlayers()) {
				damage(player, damageRange, damageType, drop);
			}
			break;
		}

	}

	private void damage(LivingEntity ent, IntRange damageRange, DamageType damageType, CustomDrop drop) {
		Log.logInfo("Damaging player: "+ent.toString()+" "+damageRange.toString()+"("+damageType.toString()+")");
		switch (damageType) {
		case NORMAL:
			ent.damage(damageRange.getRandomIn(OtherDrops.rng));
			break;
		case FIRE:
			ent.setFireTicks(damageRange.getRandomIn(OtherDrops.rng));
			break;
		case LIGHTNING:
			Location location = ent.getLocation().clone();
			if (drop instanceof SimpleDrop) location = ((SimpleDrop)drop).getRandomisedLocation(location);
			int damageVal = damageRange.getRandomIn(OtherDrops.rng);
			World world = location.getWorld();
			
			if (damageVal == 0) world.strikeLightningEffect(location);
			else world.strikeLightning(location);

			break;
		}
		
	}

	//@Override
	static public List<Action> parse(ConfigurationNode parseMe) {
		List<Action> actions = new ArrayList<Action>();

		for (String key : matches.keySet()) {
			if (parseMe.get(key) != null)
				actions.add(new DamageAction(parseMe.get(key), matches.get(key)));
		}


		return actions;
	}


}
