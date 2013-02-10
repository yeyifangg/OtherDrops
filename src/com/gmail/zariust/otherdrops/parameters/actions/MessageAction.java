package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ProjectileAgent;

public class MessageAction extends Action {
	// message.player, message.radius@<r>, message.world, message.server
	public enum MessageType {
		ATTACKER, VICTIM, RADIUS, WORLD, SERVER
	}
	
	static Map<String, MessageType> matches = new HashMap<String, MessageType>();
	static {
		matches.put("message", MessageType.ATTACKER);
		matches.put("message.attacker", MessageType.ATTACKER);
		matches.put("message.victim", MessageType.VICTIM);
		matches.put("message.server", MessageType.SERVER);
		matches.put("message.world", MessageType.WORLD);
		matches.put("message.global", MessageType.SERVER);
		matches.put("message.all", MessageType.SERVER);
	}

	protected MessageType messageType;
	protected double radius;
	private List<String> messages; // this can contain variables, parse at runtime
	
	public MessageAction(Object messageToParse, MessageType messageType2) {
		this(messageToParse, messageType2, 0);
	}
	
	public MessageAction(Object messageToParse, MessageType messageType2, double radius) {
		if (messageToParse instanceof List)	messages = (List<String>) messageToParse;
		else messages = Collections.singletonList(messageToParse.toString());
		
		//OtherDrops.logInfo("Adding messages: "+messages.toString());

		messageType = messageType2;
		this.radius = radius;
		
	}

	@Override
	public boolean act(CustomDrop drop, OccurredEvent occurence) {
		String message = getRandomMessage(drop, occurence, this.messages);
		
		Log.logInfo("Message action - messages = "+messages.toString()+", message="+message+", type="+messageType.toString(), Verbosity.HIGH);

		switch (messageType) {
		case ATTACKER:
			if (occurence.getPlayerAttacker() != null)
				occurence.getPlayerAttacker().sendMessage(message);
			break;
		case VICTIM:
			if (occurence.getPlayerVictim() != null)
				occurence.getPlayerVictim().sendMessage(message);
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
									player.sendMessage(message);
			}
			
			break;
		case SERVER:
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				player.sendMessage(message);
			}
			break;
		case WORLD:
			for (Player player : occurence.getLocation().getWorld().getPlayers()) {
				player.sendMessage(message);
			}
			break;
		}
		return false;
	}

	//@Override
	static public List<Action> parse(ConfigurationNode parseMe) {		
		List<Action> actions = new ArrayList<Action>();
		
		for (String key : matches.keySet()) {
			if (parseMe.get(key) != null)
				actions.add(new MessageAction(parseMe.get(key), matches.get(key)));
		}
		//messages = OtherDropsConfig.getMaybeList(new ConfigurationNode((Map<?, ?>)parseMe), "message", "messages");
		return actions;
	}

	static public String getRandomMessage(CustomDrop drop, OccurredEvent occurence, List<String> messages) {
		double amount = occurence.getCustomDropAmount();
		if(messages == null || messages.isEmpty()) return "";
		String msg = messages.get(drop.rng.nextInt(messages.size()));
		msg = parseVariables(msg, drop, occurence, amount);
		return (msg == null) ? "" : msg;
	}

	static public String parseVariables(String msg, CustomDrop drop, OccurredEvent occurence, double amount) {
		if (msg == null) return msg;
		
		msg = msg.replace("%Q", "%q");
		if(drop instanceof SimpleDrop) {
			if (((SimpleDrop)drop).getDropped() != null) {
				if(((SimpleDrop)drop).getDropped().isQuantityInteger())
					msg = msg.replace("%q", String.valueOf(Math.round(amount)));
				else msg = msg.replace("%q", Double.toString(amount));
			}
		}
		msg = msg.replace("%d", drop.getDropName().replaceAll("[_-]", " ").toLowerCase());
		msg = msg.replace("%D", drop.getDropName().replaceAll("[_-]", " ").toUpperCase());

		String toolName = occurence.getTool().toString();
		String playerName = "";
		if (occurence.getTool() instanceof PlayerSubject) {
			toolName = ((PlayerSubject)occurence.getTool()).getTool().getMaterial().toString().replaceAll("[_-]", " ");
			playerName = ((PlayerSubject)occurence.getTool()).getPlayer().getName();
		} else if (occurence.getTool() instanceof ProjectileAgent) {
			if (((ProjectileAgent)occurence.getTool()).getShooter() == null) {
				Log.logInfo("MessageAction: getShooter = null, this shouldn't happen. ("+occurence.getTool().toString()+")");
				playerName = "null";
			} else {
				playerName = ((ProjectileAgent)occurence.getTool()).getShooter().getReadableName();
			}
			toolName = occurence.getTool().getReadableName();
		}
		msg = msg.replace("%t", toolName.toLowerCase());
		msg = msg.replace("%T", toolName.toUpperCase());
		
		msg = msg.replace("%v", occurence.getTarget().getReadableName());
		
		msg = msg.replace("%p", playerName);
		msg = msg.replace("%P", playerName.toUpperCase());
	
		msg = msg.replaceAll("&([0-9a-fA-F])", "§$1"); 	// replace color codes
		msg = msg.replaceAll("&([kKlLmMnNoOrR])", "§$1");               // replace magic color code & others

		//Magic (random characters): &k
		//Bold: &l
		//Strikethrough: &m
		//Underline: &n
		//Italic: &o
		//Reset: &r

		msg = msg.replace("&&", "&"); 					// replace "escaped" ampersand
		
		return msg;
	}
}
