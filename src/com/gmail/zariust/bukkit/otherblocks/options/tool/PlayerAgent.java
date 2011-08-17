package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class PlayerAgent extends Agent {
	private MaterialData mat;
	private Player agent;
	
	public PlayerAgent(Material tool) {
		this(tool, 0);
	}
	
	public PlayerAgent(Material tool, int data) {
		this(tool.getData() == null ? new MaterialData(tool) : tool.getNewData((byte) data));
	}
	
	public PlayerAgent(MaterialData tool) {
		super(ToolType.ITEM);
		mat = tool;
	}
	
	public PlayerAgent(Player attacker) {
		this(attacker.getItemInHand().getType(), attacker.getItemInHand().getDurability());
		agent = attacker;
	}
	
	@Override
	protected boolean matches(Agent other) {
		if(other instanceof PlayerAgent) return matches((PlayerAgent) other);
		return false;
	}
	
	private boolean matches(PlayerAgent other) {
		if(mat == null || other.mat == null) return true;
		boolean materialEqual = mat.equals(other.mat);
		if(agent == null | other.agent == null) return materialEqual;
		return materialEqual && agent.equals(other.agent);
	}
	
	@Override
	protected int getIdHash() {
		return mat == null ? 0 : mat.getItemTypeId();
	}
	
	@Override
	protected int getDataHash() {
		return mat == null ? 0 : mat.getData();
	}
	
	public MaterialData getMaterial() {
		return mat;
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
