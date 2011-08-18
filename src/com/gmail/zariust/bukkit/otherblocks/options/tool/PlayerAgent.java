package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class PlayerAgent extends Agent {
	private Integer id, data;
	private Player agent;
	
	public PlayerAgent() {
		this((Material) null);
	}
	
	public PlayerAgent(Material tool) {
		this(tool, null);
	}
	
	public PlayerAgent(Material tool, Integer d) {
		this(tool == null ? null : tool.getId(), d);
	}
	
	public PlayerAgent(MaterialData tool) {
		this(tool == null ? null : tool.getItemType(), tool == null ? null : (int) tool.getData());
	}
	
	public PlayerAgent(Integer tool, Integer type) {
		super(ToolType.ITEM);
		id = tool;
		data = type;
	}
	
	public PlayerAgent(Player attacker) {
		this(attacker.getItemInHand().getType(), (int) attacker.getItemInHand().getDurability());
		agent = attacker;
	}
	
	private PlayerAgent equalsHelper(Object other) {
		if(!(other instanceof PlayerAgent)) return null;
		return (PlayerAgent) other;
	}

	private boolean isEqual(PlayerAgent tool) {
		if(tool == null) return false;
		return id == tool.id && data == tool.data;
	}

	@Override
	public boolean equals(Object other) {
		PlayerAgent tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		PlayerAgent tool = equalsHelper(other);
		if(id == null) return true;
		else if(data == null) return id == tool.id;
		else return isEqual(tool);
	}
	
	@Override
	protected int getIdHash() {
		return id == null ? 0 : id;
	}
	
	@Override
	protected int getDataHash() {
		return data == null ? 0 : data;
	}
	
	public Material getMaterial() {
		return Material.getMaterial(id);
	}
	
	@Override
	public void damageTool(short damage) {
		if(damage == 0) return;
		ItemStack stack = agent.getItemInHand();
		if(stack == null || stack.getAmount() == 1) {
			agent.setItemInHand(null);
			return;
		}
		// Either it's a tool and we damage it, or it's not and we take one
		short maxDurability = stack.getType().getMaxDurability();
		if(maxDurability > 0) { // a tool
			short durability = stack.getDurability();
			if(durability + damage >= maxDurability) agent.setItemInHand(null);
			else stack.setDurability((short) (durability + damage));
		} else { // not a tool
			int amount = stack.getAmount();
			if(amount <= damage) agent.setItemInHand(null);
			else stack.setAmount(amount - damage);
		}
		// TODO: Option of failure if damage is greater that the amount remaining?
	}
	
	@Override
	public void damageTool() { // Default tool damage; 1 for tools, 0 for normal items
		if(agent.getItemInHand().getType().getMaxDurability() > 0) damageTool((short) 1);
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}
}
