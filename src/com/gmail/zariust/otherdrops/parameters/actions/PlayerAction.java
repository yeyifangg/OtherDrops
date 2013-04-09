package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.parameters.Action;

public class PlayerAction extends ActionMulti {

    public enum StatType {
        HUNGER, XP, SPEED
    }

    protected double       radius = 10; // default
                                        // to
                                        // 10
                                        // blocks
    private final StatType stat;
    private float          statValue;

    public PlayerAction(StatType stat, Object object, ActionType actionType) {
        this.stat = stat;
        this.actionType = actionType;

        if (object instanceof String) {
            statValue = Float.valueOf((String) object);
        } else if (object instanceof Integer) {
            statValue = Float.valueOf(((Integer) object).toString());
        } else if (object instanceof Float) {
            statValue = (Float) object;
        } else if (object instanceof Double) {
            statValue = ((Double) object).floatValue();
        }
    }

    @Override
    protected void applyEffect(LivingEntity lEnt) {
        if (lEnt == null) {
            return;
        }

        if (lEnt instanceof Player) {
            Player player = (Player) lEnt;

            switch (stat) {
            case HUNGER:
                player.setFoodLevel(Math.round(statValue));
                break;
            case SPEED:
                Log.dMsg("Setting walk speed to: " + statValue);
                player.setWalkSpeed(statValue);
                break;
            case XP:
                player.giveExp(Math.round(statValue));
                break;
            default:
                break;

            }
        }
    }

    @Override
    public List<Action> parse(ConfigurationNode parseMe) {
        List<Action> actions = new ArrayList<Action>();

        // foodlevel, flyspeed, flight, level, saturation, walkspeed,
        Map<String, ActionType> matches = getMatches("pset.hunger");
        actions.addAll(parse(parseMe, matches, StatType.HUNGER));

        matches = getMatches("pset.xp");
        actions.addAll(parse(parseMe, matches, StatType.XP));

        matches = getMatches("pset.speed");
        actions.addAll(parse(parseMe, matches, StatType.SPEED));

        return actions;
    }

    private Collection<? extends Action> parse(ConfigurationNode parseMe,
            Map<String, ActionType> matches, StatType stat) {
        List<Action> actions = new ArrayList<Action>();
        if (parseMe == null || matches == null || stat == null) {
            return actions;
        }

        for (String key : matches.keySet()) {
            if (parseMe.get(key) != null) {
                actions.add(new PlayerAction(stat, parseMe.get(key), matches
                        .get(key)));
            }
        }

        return actions;

    }
}
