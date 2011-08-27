package com.gmail.zariust.otherdrops.data;

import java.util.Random;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.OtherBlocks;
import com.gmail.zariust.otherdrops.options.IntRange;

import org.bukkit.DyeColor;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.material.MaterialData;

public class RangeData implements Data {
	private IntRange range;
	private Integer val;
	private Random rng;
	
	public RangeData(int lo, int hi, Random rand) {
		range = new IntRange(lo, hi);
		rng = rand;
	}
	
	public RangeData(IntRange r, Random rand) {
		range = r;
		rng = rand;
	}
	
	@Override
	public int getData() {
		denullifyVal();
		return val;
	}

	private void denullifyVal() {
		if(val == null) val = range.getRandomIn(rng);
	}
	
	@Override
	public void setData(int d) {
		val = d;
	}
	
	@Override
	public boolean matches(Data d) {
		// TODO: Allow range to match other sorts of data?
		// I don't think other sorts really work though.
		if(!(d instanceof RangeableData)) return false;
		return range.contains(d.getData());
	}
	
	@Override
	public String get(Enum<?> mat) {
		return "RANGE-" + range.toString();
	}
	
	@Override
	public void setOn(BlockState state) {
		denullifyVal();
		state.setData(new MaterialData(state.getType(), val.byteValue()));
	}
	
	@Override
	public void setOn(Entity mob, Player witness) {
		denullifyVal();
		switch(CommonEntity.getCreatureType(mob)) {
		case SHEEP:
			if(val >= 32) ((Sheep)mob).setSheared(true);
			val -= 32;
			if(val > 0) ((Sheep)mob).setColor(DyeColor.getByData((byte) (val - 1)));
			break;
		case SLIME:
			if(val > 0) ((Slime)mob).setSize(val);
			break;
		case PIG_ZOMBIE:
			if(val > 0) ((PigZombie)mob).setAnger(val);
			break;
		default:
		}
	}

	public static Data parse(String state) {
		state = state.toUpperCase().replace("RANGE-", "");
		return new RangeData(IntRange.parse(state), OtherBlocks.rng);
	}
	
	public IntRange getRange() {
		return range;
	}
	
	void setRange(IntRange newRange) {
		range = newRange;
	}
	
	void setRange(int lo, int hi) {
		setRange(new IntRange(lo,hi));
	}
	
}
