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

package com.gmail.zariust.otherdrops.drop;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.zariust.common.CMEnchantment;
import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.Target;

public class ItemDrop extends DropType {
    private final Material            material;
    private final Data                durability;
    private final IntRange            quantity;
    private int                       rolledQuantity;
    private final List<CMEnchantment> enchantments;

    public ItemDrop(Material mat) {
        this(mat, 100.0);
    }

    public ItemDrop(Material mat, int data) {
        this(mat, data, 100.0);
    }

    public ItemDrop(IntRange amount, Material mat) {
        this(amount, mat, 100.0, null);
    }

    public ItemDrop(IntRange amount, Material mat, int data) {
        this(amount, mat, data, 100.0, null, "");
    }

    public ItemDrop(ItemStack stack) {
        this(stack, 100.0);
    }

    public ItemDrop(Material mat, double percent) {
        this(mat, 0, percent);
    }

    public ItemDrop(Material mat, int data, double percent) {
        this(mat == null ? null : new ItemStack(mat, 1, (short) data), percent);
    }

    public ItemDrop(IntRange amount, Material mat, double percent,
            List<CMEnchantment> enchantment, String loreName) {
        this(amount, mat, 0, percent, enchantment, loreName);
    }

    public ItemDrop(IntRange amount, Material mat, double percent,
            List<CMEnchantment> enchantment) {
        this(amount, mat, 0, percent, enchantment, "");
    }

    public ItemDrop(IntRange amount, Material mat, int data, double percent,
            List<CMEnchantment> enchantment, String loreName) {
        this(amount, mat, new ItemData(data), percent, enchantment, loreName,
                null);
    }

    public ItemDrop(ItemStack stack, double percent) {
        this(new IntRange(stack == null ? 1 : stack.getAmount()),
                stack == null ? null : stack.getType(), stack == null ? null
                        : new ItemData(stack), percent, null, "", null);
    }

    public ItemDrop(IntRange amount, Material mat, Data data, double percent,
            List<CMEnchantment> enchPass, String loreName, List<String> loreList) { // Rome
        super(DropCategory.ITEM, percent);
        quantity = amount;
        material = mat;
        durability = data;
        this.enchantments = enchPass;
        this.displayName = loreName;
        this.lore = loreList;
    }

    /**
     * Return an ItemStack that represents this item
     * 
     * @return
     */
    public ItemStack getItem() {
        return getItem(null);
    }

    public ItemStack getItem(Target source) {
        short data = processTHISdata(source);
        rolledQuantity = quantity.getRandomIn(OtherDrops.rng);
        ItemStack stack = new ItemStack(material, rolledQuantity, data);
        stack = CommonEnchantments.applyEnchantments(stack, enchantments);
        setItemMeta(stack, source);
        return stack;
    }

    @Override
    protected DropResult performDrop(Target source, Location where,
            DropFlags flags) {
        DropResult dropResult = DropResult
                .getFromOverrideDefault(this.overrideDefault);
        if (material == null || quantity.getMax() == 0)
            return dropResult;
        // Material AIR = drop NOTHING so always override
        if (material == Material.AIR)
            dropResult.setOverrideDefault(true);

        ItemStack stack = getItem(source); // get the item stack with relevant
                                           // enchantments and/or metadata
        int count = 1; // if DropSpread is false we drop a single (multi-item)
                       // stack

        if (flags.spread) { // if DropSpread is true, then
            stack.setAmount(1); // set amount to 1 as we're going to drop single
                                // items one by one
            count = rolledQuantity; // set #times to drop = #items to be dropped
        }

        while (count-- > 0)
            dropResult.addWithoutOverride(drop(where, stack, flags.naturally));

        setLoreName(dropResult.getDropped(), flags);
        return dropResult;
    }

    /**
     * Sets any relevant metadata on the item (currently only leather armor
     * color
     * 
     * @param stack
     * @param source
     */
    private void setItemMeta(ItemStack stack, Target source) {
        if ((durability instanceof ItemData)
                && ((ItemData) durability).itemMeta != null) {
            stack = ((ItemData) durability).itemMeta.setOn(stack, source);
        }
    }

    /**
     * Check if data is THIS (-1) and get "self-data" accordingly
     * 
     * @param source
     * @return data as a short (for use in an ItemStack)
     */
    private short processTHISdata(Target source) {
        int itemData = durability.getData();
        if (itemData == -1) { // ie. itemData = THIS
            if (source == null)
                return (short) 0;
            String[] dataSplit = source.toString().split("@");
            if (material.toString().equalsIgnoreCase("monster_egg")) { // spawn
                                                                       // egg
                EntityType creatureType = CommonEntity
                        .getCreatureEntityType(dataSplit[0]);
                if (creatureType != null)
                    itemData = creatureType.getTypeId();
            } else {
                if (dataSplit.length > 1)
                    itemData = ItemData.parse(material,
                            dataSplit[1].replaceAll("SHEARED/", "")).getData(); // for
                                                                                // wool,
                                                                                // logs,
                                                                                // etc
            }
            if (itemData == -1)
                itemData = 0; // reset to default data if we weren't able to
                              // parse anything else
        }
        return (short) itemData;
    }

    /**
     * Sets lore name and (soon to be) description on the spawned item(s)
     * 
     * @param flags
     * 
     * @param dropResult
     */
    private void setLoreName(List<Entity> entityList, DropFlags flags) {
        if (entityList != null && !(displayName.isEmpty())) {
            for (Entity ent : entityList) {
                Item is = (Item) ent;
                ItemMeta im = is.getItemStack().getItemMeta();

                String victimName = ""; // TODO: fix these
                String parsedLoreName = MessageAction.parseVariables(
                        displayName, flags.getRecipientName(), victimName,
                        this.getName(), flags.getToolName(),
                        String.valueOf(this.rolledQuantity), "", "");
                im.setDisplayName(parsedLoreName);
                if (lore != null) {
                    List<String> parsedLore = new ArrayList<String>();
                    for (String line : lore) {
                        parsedLore.add(MessageAction.parseVariables(line,
                                flags.getRecipientName(), victimName,
                                this.getName(), flags.getToolName(),
                                String.valueOf(this.rolledQuantity), "", ""));
                    }
                    im.setLore(parsedLore);
                }
                is.getItemStack().setItemMeta(im);
            }
        }
    }

    static public class ODItem {
        public String               name;
        private String              dataString;
        public String               enchantmentString;
        private List<CMEnchantment> enchantments = new ArrayList<CMEnchantment>();
        private String              displayname  = "";
        private final List<String>  lore         = new ArrayList<String>();
        private Material            material;
        private Data                data;

        /**
         * @param drop
         * @param defaultData
         * @param loreName
         * @return
         */
        public static ODItem parseItem(String drop, String defaultData) {
            ODItem item = new ODItem();
            item.dataString = defaultData;

            String[] firstSplit = drop.split("[@:;~]", 2);
            if (firstSplit.length > 1) {
                // if extra fields are found, parse them - firstly separating out the type of "thing" this is
                item.name = firstSplit[0];
                String firstChar = drop.substring(item.name.length(),
                        item.name.length() + 1);
                if (firstChar.matches("[^~]")) {
                    // only want to use a semi-colon rather than @ or : but preserve the ~
                    firstChar = ";"; 
                }
                drop = firstChar + firstSplit[1];

                // check for initial data value and enchantment to support old format
                if (drop.matches("([;])([^;!]+)!.*")) {
                    Log.dMsg("PARSING INTIAL DATA");
                    String[] dataEnchSplit = drop.split("!", 2);
                    item.dataString = dataEnchSplit[0].substring(1);
                    drop = ";"+dataEnchSplit[1];
                    
                }

                // then, loop through each ";<value>" or "~<value>" pair and parse accordingly
                Pattern p = Pattern.compile("([~;])([^~;]+)");
                Matcher m = p.matcher(drop);
                while (m.find()) {
                    String key = m.group(1);
                    String value = m.group(2);

                    if (key != null && value != null) {
                        if (key.equals("~")) {
                            item.displayname = value;
                        } else if (item.displayname != null
                                && !item.displayname.isEmpty()) {
                            // displayname found, treat next as lore
                            item.lore.add(value);
                        } else {
                            // first check for enchantment
                            List<CMEnchantment> ench = CommonEnchantments
                                    .parseEnchantments(value);
                            if (ench == null || ench.isEmpty()) {
                                // otherwise assume data
                                item.dataString = value;
                            } else {
                                item.enchantments.addAll(ench);
                            }
                        }
                    }
                }
            } else {
                item.name = drop;
            }

            return item;
        }

        /**
         * @param name
         * @return
         */
        public Material getMaterial() {
            if (this.material == null) {
                try {
                    int dropInt = Integer.parseInt(this.name);
                    material = Material.getMaterial(dropInt);
                } catch (NumberFormatException e) {
                    material = CommonMaterial.matchMaterial(this.name);
                }
            }
            return this.material;
        }

        public String getDataString() {
            return (dataString == null ? "" : dataString);
        }

        /**
         * @param item
         * @param mat
         * @return
         */
        public Data getData() {
            if (data == null) {
                // Parse data, which could be an integer or an appropriate enum
                // name
                this.data = parseDataFromString(this.dataString);
            }
            return data;
        }

        /**
         * @return
         * 
         */
        public Data parseDataFromString(String dataString) {
            Data returnVal = null;
            try {
                int d = Integer.parseInt(dataString);
                returnVal = new ItemData(d);
            } catch (NumberFormatException e) {
            }
            if (returnVal == null) {
                try {
                    returnVal = ItemData.parse(this.getMaterial(), dataString);
                    if (returnVal == null)
                        returnVal = new ItemData(0);
                } catch (IllegalArgumentException e) {
                    Log.logWarning(e.getMessage());
                    returnVal = null;
                }
            }
            return returnVal;
        }

        public String getDisplayName() {
            return displayname;
        }

        public List<CMEnchantment> getEnchantments() {
            if (enchantments == null) {
                if (enchantmentString == null) {
                    enchantments = new ArrayList<CMEnchantment>();
                } else {
                    enchantments = CommonEnchantments
                            .parseEnchantments(enchantmentString);
                }
            }
            return enchantments;
        }

        public static ODItem parseItem(String blockName) {
            return parseItem(blockName, "");
        }

    }

    public static DropType parse(String drop, String defaultData,
            IntRange amount, double chance) {
        ODItem item = ODItem.parseItem(drop, defaultData);

        Material mat = item.getMaterial();
        if (mat == null)
            return null;
        Data data = item.getData();
        if (data == null)
            return null; // Data should only be null if invalid for this type,
                         // so don't continue

        return new ItemDrop(amount, mat, data, chance, item.enchantments,
                item.displayname, item.lore);
    }

    @Override
    public String getName() {
        if (material == null)
            return "DEFAULT";
        String ret = material.toString();
        // TODO: Will durability ever be null, or will it just be 0?
        if (durability != null) {
            String dataString = durability.get(material);
            if (dataString != null)
                ret += (dataString.isEmpty()) ? "" : "@"
                        + durability.get(material);
        }
        return ret;
    }

    @Override
    public double getAmount() {
        return rolledQuantity;
    }

    @Override
    public DoubleRange getAmountRange() {
        return quantity.toDoubleRange();
    }

    public Material getMaterial() {
        return material;
    }
}
