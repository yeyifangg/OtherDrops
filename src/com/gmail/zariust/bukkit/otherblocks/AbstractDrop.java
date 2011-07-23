package com.gmail.zariust.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbstractDrop {
	public List<String> worlds;
	public Integer damage;
	public Double chance;
	public List<String> messages;
	public String time;
	public List<String> weather;
	public List<String> biome;
	public List<String> event;
	public String height;
    public List<String> permissionGroups; // obseleted - use permissions
    public List<String> permissionGroupsExcept; // obseleted - use permissionsExcept
    public List<String> permissions;
    public List<String> permissionsExcept;
    public String exclusive;
    
    protected Integer attackerDamageMin;
    protected Integer attackerDamageMax;    
	
	protected static Random rng = new Random();

	// Attacker Damage
	public Integer getRandomAttackerDamage()
	{
		if (attackerDamageMin == attackerDamageMax) return attackerDamageMin;
		
		Integer randomVal = (attackerDamageMin + rng.nextInt(attackerDamageMax - attackerDamageMin + 1));
		return randomVal;
	}

	public void setAttackerDamage(Integer val) {
	    try {
	        this.setAttackerDamage(val, val);
	    } catch(NullPointerException x) {
	        this.attackerDamageMin = this.attackerDamageMax = null;
	    }
	}
	
	public void setAttackerDamage(Integer low, Integer high) {
	    if(low < high) {
	        this.attackerDamageMin = low;
	        this.attackerDamageMax = high;
	    } else {
	        this.attackerDamageMin = high;
	        this.attackerDamageMax = low;
	    }
	}
	
	public boolean isAttackerDamageValid(Short test) {
	    if(this.attackerDamageMin == null) return true;
	    return (test >= this.attackerDamageMin && test <= this.attackerDamageMax);
	}
	
}
