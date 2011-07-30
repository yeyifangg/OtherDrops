package com.gmail.zariust.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbstractDrop {
	public String original;
	public List<String> tool;
	public List<String> toolExceptions;
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
    public String delay;
    public List<String> regions;
    public List<String> replacementBlock;
    public String attackRange;
    public String lightLevel;

    protected Integer attackerDamageMin;
    protected Integer attackerDamageMax;    
	
	protected static Random rng = new Random();

	public AbstractDrop() {
		tool = new ArrayList<String>();
//		worlds = new ArrayList<String>();
//		messages = new ArrayList<String>();
//		weather = new ArrayList<String>();
//		biome = new ArrayList<String>();
//		event = new ArrayList<String>();
//		permissionGroups = new ArrayList<String>();
//		permissionGroupsExcept = new ArrayList<String>();
//		permissions = new ArrayList<String>();
//		permissionsExcept = new ArrayList<String>();
		//regions = new ArrayList<String>();
		
	}

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
