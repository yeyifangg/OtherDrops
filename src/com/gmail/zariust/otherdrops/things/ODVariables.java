package com.gmail.zariust.otherdrops.things;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import com.gmail.zariust.otherdrops.OtherDropsConfig;

public class ODVariables {
    Map<String, String> variables = new HashMap<String, String>();        

    public ODVariables() {
        variables.put("%time", new SimpleDateFormat(OtherDropsConfig.gTimeFormat).format(Calendar.getInstance().getTime()));
        variables.put("%date", new SimpleDateFormat(OtherDropsConfig.gDateFormat).format(Calendar.getInstance().getTime()));
    }

    public ODVariables setDeathMessage(String val) {
        variables.put("%deathmessage", val);
        return this;
    }

    public ODVariables setPlayerName(String val) {
        variables.put("%p", val);
        variables.put("%P", val.toUpperCase());
        return this;
    }

    public ODVariables setVictimName(String val) {
        variables.put("%v", val);
        return this;
    }

    public ODVariables setDropName(String val) {
        variables.put("%d", val.replaceAll("[_-]", " ").toLowerCase());
        variables.put("%D", val.replaceAll("[_-]", " ").toUpperCase());
        return this;
    }

    public ODVariables setToolName(String val) {
        variables.put("%t", val.replaceAll("[_-]", " ").toLowerCase());
        variables.put("%T", val.replaceAll("[_-]", " ").toUpperCase());
        return this;
    }

    public ODVariables setloreName(String val) {
        variables.put("%displayname", val);
        variables.put("%lorename", val);
        return this;
    }

    public ODVariables setQuantity(String val) {
        variables.put("%q", val);
        variables.put("%Q", val);
        return this;
    }

    public ODVariables setTargetName(String val) {
        variables.put("%targetname", val);
        return this;
    }
    
    public ODVariables custom(String key, String value) {
        variables.put(key, value);
        return this;
    }
    
    public String parse(String msg) {
        for (Entry<String, String> entrySet : variables.entrySet()) {
            msg = msg.replaceAll(entrySet.getKey(), entrySet.getValue());
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        msg = msg.replace("&&", "&"); // replace "escaped" ampersand

        return msg;
    }
    
    
    static public String parseVariables(String msg, String playerName,
            String victimName, String dropName, String toolName,
            String quantityString, String deathMessage, String loreName) {
        return new ODVariables().setPlayerName(playerName).setVictimName(victimName).setDropName(dropName)
                .setToolName(toolName).setQuantity(quantityString).setDeathMessage(deathMessage).setloreName(loreName)
                .parse(msg);
    }
    public static String parseVariables(String msg) {
        return new ODVariables().parse(msg);
    }

    public static List<String> parseVariables(List<String> stringList) {
        List<String> parsedStringList = new ArrayList<String>();
        for (String string : stringList) {
            parsedStringList.add(parseVariables(string));
        }
        return parsedStringList;
    }

}
