package com.gmail.zariust.otherdrops.parameters.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.parameters.Action;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
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
        matches.put("message.radius", MessageType.RADIUS);
    }

    protected MessageType           messageType;
    protected double                radius  = 10;
    private List<String>            messages;                                    // this
                                                                                  // can
                                                                                  // contain
                                                                                  // variables,
                                                                                  // parse
                                                                                  // at
                                                                                  // runtime

    public MessageAction(Object messageToParse, MessageType messageType2) {
        this(messageToParse, messageType2, 0);
    }

    @SuppressWarnings("unchecked")
    public MessageAction(Object messageToParse, MessageType messageType2,
            double radius) {
        if (messageToParse == null)
            return; // "Registration" passed a null value

        if (messageToParse instanceof List)
            messages = (List<String>) messageToParse;
        else
            messages = Collections.singletonList(messageToParse.toString());

        // OtherDrops.logInfo("Adding messages: "+messages.toString());

        messageType = messageType2;
        this.radius = radius;

    }

    @Override
    public boolean act(CustomDrop drop, OccurredEvent occurence) {
        String message = getRandomMessage(drop, occurence, this.messages);
        if (message.isEmpty())
            return false;

        Log.logInfo("Message action - messages = " + messages.toString()
                + ", message=" + message + ", type=" + messageType.toString(),
                Verbosity.HIGH);

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
            // occurence.getLocation().getRadiusPlayers()? - how do we get
            // players around radius without an entity?
            Location loc = occurence.getLocation();
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.getLocation().getX() > (loc.getX() - radius)
                        || player.getLocation().getX() < (loc.getX() + radius))
                    if (player.getLocation().getY() > (loc.getY() - radius)
                            || player.getLocation().getY() < (loc.getY() + radius))
                        if (player.getLocation().getZ() > (loc.getZ() - radius)
                                || player.getLocation().getZ() < (loc.getZ() + radius))
                            player.sendMessage(message);
            }

            break;
        case SERVER:
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendMessage(message);
            }
            break;
        case WORLD:
            for (Player player : occurence.getLocation().getWorld()
                    .getPlayers()) {
                player.sendMessage(message);
            }
            break;
        }
        return false;
    }

    // @Override
    @Override
    public List<Action> parse(ConfigurationNode parseMe) {
        List<Action> actions = new ArrayList<Action>();

        for (String key : matches.keySet()) {
            if (parseMe.get(key) != null)
                actions.add(new MessageAction(parseMe.get(key), matches
                        .get(key)));
        }
        // messages = OtherDropsConfig.getMaybeList(new
        // ConfigurationNode((Map<?, ?>)parseMe), "message", "messages");
        return actions;
    }

    static public String getRandomMessage(CustomDrop drop,
            OccurredEvent occurence, List<String> messages) {
        double amount = occurence.getCustomDropAmount();
        if (messages == null || messages.isEmpty())
            return "";
        String msg = messages.get(drop.rng.nextInt(messages.size()));
        msg = parseVariables(msg, drop, occurence, amount);
        return (msg == null) ? "" : msg;
    }

    static public String parseVariables(String msg) {
        return parseVariables(msg, null, null, null, null, null, "", "");
    }

    public static List<String> parseVariables(List<String> stringList) {
        List<String> parsedStringList = new ArrayList<String>();
        for (String string : stringList) {
            parsedStringList.add(parseVariables(string));
        }
        return parsedStringList;
    }

    static public String parseVariables(String msg, String playerName,
            String victimName, String dropName, String toolName,
            String quantityString, String deathMessage, String loreName) {
        if (msg == null)
            return null;

        // This prefix allows dollar signs to be escaped
        // to ignore the variable, eg. \\$time
        // TODO: find a better way to do this
        String prefix = "([^\\\\])[$%]";

        // //////////////////////////
        // Full word variables
        // Needs to be before single character variables
        msg = msg.replaceAll(
                prefix + "time",
                "$1"
                        + new SimpleDateFormat(OtherDropsConfig.gTimeFormat)
                                .format(Calendar.getInstance().getTime()));
        msg = msg.replaceAll(
                prefix + "date",
                "$1"
                        + new SimpleDateFormat(OtherDropsConfig.gDateFormat)
                                .format(Calendar.getInstance().getTime()));

        msg = msg.replaceAll(prefix + "deathmessage", "$1" + deathMessage);

        msg = msg
                .replaceAll(prefix + "(displayname|lorename)", "$1" + loreName);

        // //////////////////////////
        // Single character variables

        // $q = quantity
        msg = msg.replaceAll(prefix + "Q", "$1" + prefix + "q");
        if (quantityString != null)
            msg = msg.replaceAll(prefix + "q", "$1" + quantityString);

        // $d = drop name
        if (dropName != null) {
            msg = msg.replaceAll(prefix + "d",
                    "$1" + dropName.replaceAll("[_-]", " ").toLowerCase());
            msg = msg.replaceAll(prefix + "D",
                    "$1" + dropName.replaceAll("[_-]", " ").toUpperCase());
        }

        // $t = tool name
        if (toolName != null) {
            msg = msg.replaceAll(prefix + "t",
                    "$1" + toolName.replaceAll("[_-]", " ").toLowerCase());

            msg = msg.replaceAll(prefix + "T",
                    "$1" + toolName.replaceAll("[_-]", " ").toUpperCase());
        }

        // $v = victim name
        if (victimName != null)
            msg = msg.replaceAll(prefix + "v", "$1" + victimName);

        // $p = player name
        if (playerName != null) {
            msg = msg.replaceAll(prefix + "p", "$1" + playerName);
            msg = msg.replaceAll(prefix + "P", "$1" + playerName.toUpperCase());
        }

        // Replace /$ with $
        msg = msg.replaceAll("\\\\[$]", "\\$");

        // Search for any bracketed variables
        // Currently disabled - to be used soon for custom variables, eg
        // ${hitcount.$p}, etc.

        // Pattern pattern = Pattern.compile("[$%]\\{(.*?)\\}");
        // Matcher matcher = pattern.matcher(msg);
        // StringBuffer sb = new StringBuffer();
        // while (matcher.find()) {
        // String result = matcher.group(1).toLowerCase();
        //
        // if (result.equals("time")) {
        // matcher.appendReplacement(sb, new SimpleDateFormat(
        // OtherDropsConfig.gTimeFormat).format(Calendar
        // .getInstance().getTime()));
        //
        // } else if (result.equals("date")) {
        // matcher.appendReplacement(sb, new SimpleDateFormat(
        // OtherDropsConfig.gDateFormat).format(Calendar
        // .getInstance().getTime()));
        // }
        // }
        // matcher.appendTail(sb);
        // msg = sb.toString();

        // msg = msg.replaceAll("&([0-9a-fA-F])", "§$1"); // replace color codes
        // msg = msg.replaceAll("&([kKlLmMnNoOrR])", "§$1"); // replace magic
        // color code & others

        // //////////////////////////
        // Color codes

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        // Colors: &([0-9a-fA-F])
        // Magic (random characters): &k
        // Bold: &l
        // Strikethrough: &m
        // Underline: &n
        // Italic: &o
        // Reset: &r

        msg = msg.replace("&&", "&"); // replace "escaped" ampersand

        return msg;
    }

    static public String parseVariables(String msg, CustomDrop drop,
            OccurredEvent occurence, double amount) {
        if (msg == null)
            return msg;

        String dropName = "";
        String toolName = "";
        String playerName = "";
        String victimName = "";
        String quantityString = "";
        String deathMessage = "";
        String loreName = "";

        if (drop != null) {
            if (drop instanceof SimpleDrop) {
                if (((SimpleDrop) drop).getDropped() != null) {
                    if (((SimpleDrop) drop).getDropped().isQuantityInteger())
                        quantityString = String.valueOf(Math.round(amount));
                    else
                        quantityString = Double.toString(amount);
                }
            }
            dropName = drop.getDropName();
        }

        if (occurence != null) {
            if (occurence.getTool() != null)
                toolName = occurence.getTool().getReadableName();

            if (occurence.getTool() instanceof PlayerSubject) {
                toolName = ((PlayerSubject) occurence.getTool()).getTool()
                        .getReadableName();
                loreName = ((PlayerSubject) occurence.getTool()).getPlayer()
                        .getItemInHand().getItemMeta().getDisplayName();
                if (loreName == null)
                    loreName = toolName;
                playerName = ((PlayerSubject) occurence.getTool()).getPlayer()
                        .getName();
            } else if (occurence.getTool() instanceof ProjectileAgent) {
                toolName = occurence.getTool().getReadableName();
                if (((ProjectileAgent) occurence.getTool()).getShooter() == null) {
                    Log.logInfo("MessageAction: getShooter = null, this shouldn't happen. ("
                            + occurence.getTool().toString() + ")");
                    playerName = "null";
                } else {
                    playerName = ((ProjectileAgent) occurence.getTool())
                            .getShooter().getReadableName();
                    toolName += " shot by " + playerName;

                    Entity ent = ((ProjectileAgent) occurence.getTool())
                            .getShooter().getEntity();
                    if (ent instanceof LivingEntity) {
                        loreName = ((LivingEntity) ent).getCustomName();
                    }

                }
            } else if (occurence.getTool() instanceof CreatureSubject) {
                Entity ent = ((CreatureSubject) occurence.getTool())
                        .getEntity();
                if (ent instanceof LivingEntity) {
                    loreName = ((LivingEntity) ent).getCustomName();
                }
            }
            victimName = occurence.getTarget().getReadableName();

            if (occurence.getRealEvent() instanceof PlayerDeathEvent) {
                PlayerDeathEvent ede = (PlayerDeathEvent) occurence
                        .getRealEvent();

                deathMessage = ede.getDeathMessage();
            }
        }

        msg = parseVariables(msg, playerName, victimName, dropName, toolName,
                quantityString, deathMessage, loreName);

        return msg;
    }
}
