package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.ToolDamage;

public class PlayerSubject extends LivingSubject {
	private ToolAgent tool;
	private String name;
	private Player agent;
	
	public PlayerSubject() {
		this((String) null);
	}

	public PlayerSubject(String attacker) {
		this(null, attacker);
	}
	
	public PlayerSubject(Player attacker) {
		this(attacker.getItemInHand(), attacker.getName(), attacker);
	}
	
	public PlayerSubject(ItemStack item, String attacker) {
		this(item, attacker, null);
	}

	public PlayerSubject(ItemStack item, String who, Player attacker) {
		super(attacker);
		tool = new ToolAgent(item);
		name = who;
		agent = attacker;
	}

	private PlayerSubject equalsHelper(Object other) {
		if(!(other instanceof PlayerSubject)) return null;
		return (PlayerSubject) other;
	}

	private boolean isEqual(PlayerSubject player) {
		if(player == null) return false;
		return tool.equals(player.tool) && name.toUpperCase().equals(player.name.toUpperCase());
	}

	@Override
	public boolean equals(Object other) {
		PlayerSubject player = equalsHelper(other);
		return isEqual(player);
	}

	@Override
	public boolean matches(Subject other) {
		PlayerSubject player = equalsHelper(other);
		if(name == null) return true;
		else return isEqual(player);
	}

	@Override
	public int hashCode() {
		return new HashCode(this).get(name);
	}
	
	public Material getMaterial() {
		return tool.getMaterial();
	}
	
	public Player getPlayer() {
		if(agent == null) agent = Bukkit.getServer().getPlayer(name);
		return agent;
	}
	
	@Override@SuppressWarnings("deprecation")
	public void damageTool(ToolDamage damage, Random rng) {
		if(damage == null) return;
		ItemStack stack = agent.getItemInHand();
		if(stack == null) return;
		if(damage.apply(stack, rng)) agent.setItemInHand(null);
		else agent.updateInventory(); // because we've edited the stack directly
		// TODO: Option of failure if damage is greater that the amount remaining?
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}

	public ToolAgent getTool() {
		return tool;
	}

	@Override
	public Data getData() {
		return tool.getData();
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.PLAYER;
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}

	@Override
	public Location getLocation() {
		if(agent != null) return agent.getLocation();
		return null;
	}

	@Override
	public List<Target> canMatch() {
		return Collections.singletonList((Target) this);
	}

	@Override
	public String getKey() {
		return "PLAYER";
	}

	@Override
	public String toString() {
		if(name == null) {
			if(tool == null) return "PLAYER";
			return tool.toString();
		}
		return "PLAYER@" + name + " with "+tool.toString(); // TODO: does adding the tool here break anything?
	}

	public static PlayerSubject parse(String data) {
		if(data == null || data.isEmpty()) return new PlayerSubject();
		return new PlayerSubject(data);
	}
}
