package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.MaterialOption;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;

public class PlayerAgent implements LivingAgent, Target, MaterialOption {
	private ToolAgent tool;
	private String name;
	private Player agent;
	
	public PlayerAgent() {
		this((String) null);
	}

	public PlayerAgent(String attacker) {
		this(null, attacker);
	}
	
	public PlayerAgent(Player attacker) {
		this(attacker.getItemInHand(), attacker.getName());
		agent = attacker;
	}
	
	public PlayerAgent(ItemStack item, String attacker) {
		tool = new ToolAgent(item);
		name = attacker;
	}

	private PlayerAgent equalsHelper(Object other) {
		if(!(other instanceof PlayerAgent)) return null;
		return (PlayerAgent) other;
	}

	private boolean isEqual(PlayerAgent player) {
		if(player == null) return false;
		return tool.equals(player.tool) && name.equals(player.name);
	}

	@Override
	public boolean equals(Object other) {
		PlayerAgent player = equalsHelper(other);
		return isEqual(player);
	}

	@Override
	public boolean matches(Agent other) {
		PlayerAgent player = equalsHelper(other);
		if(name == null) return true;
		else return isEqual(player);
	}

	@Override
	public boolean matches(Target block) {
		PlayerAgent player = equalsHelper(block);
		if(name == null) return true;
		else return isEqual(player);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return AbstractDrop.hashCode(ItemType.PLAYER, name.hashCode(), tool.hashCode());
	}
	
	@Override
	public Material getMaterial() {
		return tool.getMaterial();
	}

	@Override
	public int getMaterialId() {
		return tool.getMaterialId();
	}
	
	public Player getPlayer() {
		if(agent == null) agent = Bukkit.getServer().getPlayer(name);
		return agent;
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

	public ToolAgent getTool() {
		return tool;
	}

	@Override
	public int getData() {
		return tool.getData();
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}
}
