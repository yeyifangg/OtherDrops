package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class PotionAction extends Action {
// "potioneffect: "
	
		private Collection<PotionEffect> effects = new ArrayList<PotionEffect>();

		public PotionAction(Collection<PotionEffect> effectsList) {
			this.effects = effectsList;
		}

		@Override
		public boolean act(CustomDrop drop, OccurredEvent occurence) {
			if (occurence.getPlayerAttacker() != null & this.effects != null)
				occurence.getPlayerAttacker().addPotionEffects(this.effects);
			return false;
		}

		//@Override
		static public List<Action> parse(ConfigurationNode parseMe) {		
			List<Action> actions = new ArrayList<Action>();

			Object effects = parseMe.get("potioneffect");
			if (effects == null) effects = parseMe.get("potioneffects");
			if (effects == null) return null;
			
			Collection<PotionEffect> effectsList = new ArrayList<PotionEffect>();
				
			if (effects instanceof List) {
				List<String> stringList = (List<String>)effects;
				for (String effect : stringList) {
					PotionEffect singleEffect = getEffect(effect);
					if (singleEffect != null) effectsList.add(singleEffect);
				}
			} else if (effects instanceof String) {
				PotionEffect singleEffect = getEffect((String)effects);
				if (singleEffect != null) effectsList.add(singleEffect);
			}
			actions.add(new PotionAction(effectsList));
			return actions;
		}

		private static PotionEffect getEffect(String effects) {			
			String[] split = effects.split("@");
			int duration = 100;
			int strength = 5;
			
			if (split.length > 1)
				duration = Integer.parseInt(split[1]);
			if (split.length > 2)
				strength = Integer.parseInt(split[2]);
			
			PotionEffectType effect = PotionEffectType.getByName(split[0]);
			if (effect == null) {
				Log.logInfo("PotionEffect: INVALID effect ("+split[0]+")", Verbosity.NORMAL);
				return null;
			}
			Log.logInfo("PotionEffect: adding effect ("+split[0]+", duration: "+duration+", strength: "+strength+")", Verbosity.HIGH);

			// FIXME: parse time and modifier
			return new PotionEffect(effect, duration, strength);
		}
	
}
