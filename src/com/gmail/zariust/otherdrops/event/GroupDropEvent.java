// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.event;

import java.util.Map;

import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class GroupDropEvent extends CustomDropEvent {
	private String name;
	private DropsList list = null;

	public GroupDropEvent(Target targ, Action act) {
		super(targ, act);
		setDrops(new DropsList());
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String getDropName() {
		return "Dropgroup " + name;
	}

	public void setDrops(DropsList drops) {
		this.list = drops;
	}

	public DropsList getDrops() {
		return list;
	}
	
	public void add(CustomDropEvent drop) {
		list.add(drop);
	}

	@Override
	public boolean isDefault() {
		return false;
	}
	
	public void sort() {
		list.sort();
	}

	@Override
	public void run() {
		Map<String,ExclusiveKey> exclusives = CustomDropEvent.newExclusiveMap(list);
		for(CustomDropEvent drop : list) {
			if(!drop.matches(currentEvent)) continue;
			if(drop.willDrop(exclusives)) drop.perform(currentEvent);
		}
	}
}
