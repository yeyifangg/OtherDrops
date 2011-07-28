package com.gmail.zariust.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.nijiko.permissions.PermissionHandler;

public class OBContainer_Drops extends AbstractDrop {
	public List<OB_Drop> list = null;

    public OBContainer_Drops() {
    	list = new ArrayList<OB_Drop>();
    }
	
    public String name;



}
