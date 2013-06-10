package com.gmail.zariust.otherdrops.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.junit.Test;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.OtherDropsConfigTest;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.ItemDrop;
import com.gmail.zariust.otherdrops.parameters.Trigger;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;

public class CustomDropTest {
    // Test target parsing
    @Test
    public void testParseTargets() {
        // Initialization: Bukkit must have a server with a logger.
        Bukkit.setServer(OtherDropsConfigTest.getServer());
        // needs verbosity
        OtherDropsConfig.setVerbosity(Verbosity.EXTREME);

        // Simple tests first:
        // Test all materials
        for (Material mat : Material.values()) {
            if (mat.isBlock() && !mat.toString().equals("WATER")) {
                String key = mat.toString();
                if (key.equals("SKULL")) key = "SKULL_BLOCK"; // deliberately realiased
                
                Target newTarg = OtherDropsConfig.parseTarget(key);
                assertTrue("Error, target (" + key + ") is null.", newTarg != null);
                assertTrue("Error, target (" + key + ") is not a blocktarget.",
                        newTarg instanceof BlockTarget);            
            }
        }
        
        // Test all entities
        for (EntityType type : EntityType.values()) {
            String key = type.toString();
            if (!type.isAlive())
                key = "ENTITY_"+key;
            
            if (key.equals("PLAYER")) continue; // PLAYER is not a creaturesubject

            Target newTarg = OtherDropsConfig.parseTarget(key);
            assertTrue("Error, target (" + key + ") is null.", newTarg != null);
            assertTrue("Error, target (" + key + ") is not a creaturesubject.",
                    newTarg instanceof CreatureSubject);            
        }
        
        // Creature Targets. Test reasons:
        List<String> testValues = Arrays.asList("IRON_GoLEM", // testing without
                                                              // CREATURE_
                "CREaTURE_CAVE_SPIDER", // testing with CREATURE_
                "CAvESPIdER", // testing with no underscores or CREATURE_
                "CREEPER@POWERED", // testing data values
                "MOO_SH rOOM", // testing aliases
                "SKELETON@WITHER", // testing witherskeletons
                "WITHERSKELETON", // testing witherskeleton by alias
                "ZOMBIE@EIBMOZ" // testing creature with invalid data
        );
        Target newTarg = null;
        for (String key : testValues) {
            newTarg = OtherDropsConfig.parseTarget(key);
            assertTrue("Error, target (" + key + ") is null.", newTarg != null);
            assertTrue("Error, target (" + key + ") is not a creaturesubject.",
                    newTarg instanceof CreatureSubject);
        }
        // Test an invalid creature
        newTarg = OtherDropsConfig.parseTarget("INVALID_CREATURE");
        assertTrue("Error, target (INVALID_CREATURE) is not null.",
                newTarg == null);

        // Block Targets. Test reasons:
        // DIRT = just a standard test for parsing block targets
        testValues = Arrays.asList("CROPS@0-6", "DIrT", "LeAVES@3", "LEAVES@JUnGLE", "3",
                "3@5", "LEavES:3", "3:3", "35@ReD");
        newTarg = null;
        for (String key : testValues) {
            newTarg = OtherDropsConfig.parseTarget(key);
            assertTrue("Error, target (" + key + ") is null.", newTarg != null);
            assertTrue("Error, target (" + key + ") is not a block target.",
                    newTarg instanceof BlockTarget);
        }
        newTarg = OtherDropsConfig.parseTarget("INVALID_TARGET");
        assertTrue("Error, target (INVALID_TARGET) is not null.",
                newTarg == null);

        // Test reasons:
        // PLAYER
        testValues = Arrays.asList("PLAyER");
        Target playerTarg = null;
        for (String key : testValues) {
            newTarg = OtherDropsConfig.parseTarget(key);
            assertTrue("Error, target (" + key + ") is null.", newTarg != null);
            assertTrue("Error, target (" + key + ") is not a playersubject.",
                    newTarg instanceof PlayerSubject);
        }
    }

    // Test drop type parsing
    @Test
    public void testParseDropType() {
        // Initialization: Bukkit must have a server with a logger.
        Bukkit.setServer(OtherDropsConfigTest.getServer());


        // needs verbosity
        OtherDropsConfig.setVerbosity(Verbosity.EXTREME);

        // Itemdrops. Test reasons:
        // FISH = alias for raw_fish
        // EGG = can be considered an entity or item, need to ensure it's an
        // item
        List<String> testValues = Arrays.asList("STONE_SwoRD", "FIsH", "EGG",
                "DIAmoND_SWORD@56!DAMagE_ALL#1-5~Lorename");
        DropType dropType = null;
        for (String key : testValues) {
            dropType = DropType.parse(key, "");
            assertTrue("Error, target (" + key + ") is null.", dropType != null);
            assertTrue("Error, target (" + key + ") is not an itemdrop.",
                    dropType instanceof ItemDrop);
        }
        // Test an invalid item:
        dropType = DropType.parse("INVALID_ITEM", "");
        assertTrue("Error, target (INVALID_ITEM) is not null.",
                dropType == null);

        // Lorename tests
        testValues = Arrays.asList("STONE_SWORD~&aLore name",
                "FISH@~&aLore name", "EGG@!~&aLore name",
                "DIAMONDSWORD@1-60!DAMAGE_ALL~&aLore name");
        dropType = null;
        for (String key : testValues) {
            dropType = DropType.parse(key, "");
            assertTrue("Error, target (" + key + ") is null.", dropType != null);
            assertTrue("Error, target (" + key + ") is not an itemdrop.",
                    dropType instanceof ItemDrop);
            assertTrue(
                    "Error, target (" + key + "), lorename ("
                            + dropType.getDisplayName()
                            + ") is not '&aLore name'.", dropType
                            .getDisplayName().equals("&aLore name"));
        }
    }

    // Test world conditions
    @Test
    public void testIsWorld() {
        World thisWorld = getTestWorld_TestWorld(); // named TestWorld
        World notThisWorld = getTestWorld_SecondWorld(); // named SecondWorld

        CustomDrop customDrop = new SimpleDrop(new BlockTarget(), Trigger.BREAK);
        Map<World, Boolean> worlds = new HashMap<World, Boolean>();

        // Test with a true match
        worlds.put(null, false); // ALL = false
        worlds.put(thisWorld, true);

        customDrop.setWorlds(worlds);
        assertTrue(customDrop.isWorld(thisWorld));

        // Test with a negative condition
        worlds.put(thisWorld, false); // -TestWorld
        worlds.put(null, true); // ALL = true (this gets set true for negative
                                // conditions)

        customDrop.setWorlds(worlds);
        assertTrue(customDrop.isWorld(notThisWorld));

        // Test with a false match
        worlds.put(null, false); // ALL = false
        worlds.put(notThisWorld, true); // [SecondWorld]
        customDrop.setWorlds(worlds);

        assertFalse(customDrop.isWorld(thisWorld)); // should not match
                                                    // "TestWorld"
    }

    @Test
    public void testIsRegion() {
        CustomDrop customDrop = new SimpleDrop(new BlockTarget(), Trigger.BREAK);

        // needs verbosity
        OtherDropsConfig.setVerbosity(Verbosity.EXTREME);

        Map<String, Boolean> areas = new HashMap<String, Boolean>();
        areas.put("testinside", true);
        areas.put("testinside1", true);
        areas.put("testinside2", true);
        // areas.put(null, false); // means not "all" or "any" condition
        customDrop.setRegions(areas);

        Set<String> inRegions = new HashSet<String>();
        inRegions.add("realregion");
        inRegions.add("realregion1");
        inRegions.add("testinside2");

        // test a position match
        assertTrue(customDrop.isRegion(inRegions));

        // test a negative match - this should fail as we are inside the region
        areas.put("-testinside2", false);
        // areas.put(null, true); // set true on negative conditions
        customDrop.setRegions(areas);
        assertFalse(customDrop.isRegion(inRegions));
    }

    public static World testWorld   = getTestWorld_TestWorld();
    public static World secondWorld = getTestWorld_SecondWorld();

    private static World getTestWorld_TestWorld() {
        // TODO Auto-generated method stub
        return new World() {
            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return "TestWorld";
            }

            @Override
            public boolean canGenerateStructures() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(Location arg0, float arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(Location arg0, float arg1,
                    boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3, boolean arg4) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Item dropItem(Location arg0, ItemStack arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Item dropItemNaturally(Location arg0, ItemStack arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean generateTree(Location arg0, TreeType arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean generateTree(Location arg0, TreeType arg1,
                    BlockChangeDelegate arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowAnimals() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowMonsters() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Biome getBiome(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getBlockAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getBlockAt(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getBlockTypeIdAt(Location arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Chunk getChunkAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk getChunkAt(Block arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk getChunkAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Difficulty getDifficulty() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1,
                    boolean arg2, boolean arg3) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Entity> getEntities() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T extends Entity> Collection<T> getEntitiesByClass(
                    Class<T>... arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T extends Entity> Collection<T> getEntitiesByClass(
                    Class<T> arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Environment getEnvironment() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getFullTime() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public ChunkGenerator getGenerator() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getHighestBlockAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getHighestBlockAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getHighestBlockYAt(Location arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getHighestBlockYAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public double getHumidity(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean getKeepSpawnInMemory() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<LivingEntity> getLivingEntities() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk[] getLoadedChunks() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMaxHeight() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean getPVP() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<Player> getPlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<BlockPopulator> getPopulators() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getSeaLevel() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getSeed() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Location getSpawnLocation() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public double getTemperature(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getThunderDuration() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTicksPerAnimalSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTicksPerMonsterSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTime() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public UUID getUID() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getWeatherDuration() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public File getWorldFolder() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public WorldType getWorldType() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasStorm() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isAutoSave() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isChunkLoaded(Chunk arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isChunkLoaded(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isThundering() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void loadChunk(Chunk arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void loadChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean loadChunk(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void playEffect(Location arg0, Effect arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void playEffect(Location arg0, Effect arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T> void playEffect(Location arg0, Effect arg1, T arg2,
                    int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean refreshChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean regenerateChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void save() {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAutoSave(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setDifficulty(Difficulty arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setFullTime(long arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setKeepSpawnInMemory(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setPVP(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setSpawnFlags(boolean arg0, boolean arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setStorm(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setThunderDuration(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setThundering(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTicksPerAnimalSpawns(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTicksPerMonsterSpawns(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTime(long arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setWeatherDuration(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T extends Entity> T spawn(Location arg0, Class<T> arg1)
                    throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Arrow spawnArrow(Location arg0, Vector arg1, float arg2,
                    float arg3) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LivingEntity spawnCreature(Location arg0, EntityType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LivingEntity spawnCreature(Location arg0, CreatureType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LightningStrike strikeLightning(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LightningStrike strikeLightningEffect(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean unloadChunk(Chunk arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1, boolean arg2,
                    boolean arg3) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunkRequest(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Set<String> getListeningPluginChannels() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public List<MetadataValue> getMetadata(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasMetadata(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void removeMetadata(String arg0, Plugin arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setMetadata(String arg0, MetadataValue arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setBiome(int arg0, int arg1, Biome arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3, boolean arg4, boolean arg5) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public int getAmbientSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getAnimalSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getGameRuleValue(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getGameRules() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMonsterSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getWaterAnimalSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean isChunkInUse(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isGameRule(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void playSound(Location arg0, Sound arg1, float arg2,
                    float arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAmbientSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAnimalSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean setGameRuleValue(String arg0, String arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setMonsterSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setWaterAnimalSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public Entity spawnEntity(Location arg0, EntityType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FallingBlock spawnFallingBlock(Location arg0, Material arg1,
                    byte arg2) throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FallingBlock spawnFallingBlock(Location arg0, int arg1,
                    byte arg2) throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

    private static World getTestWorld_SecondWorld() {
        // TODO Auto-generated method stub
        return new World() {
            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return "SecondWorld";
            }

            @Override
            public boolean canGenerateStructures() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(Location arg0, float arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(Location arg0, float arg1,
                    boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3, boolean arg4) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Item dropItem(Location arg0, ItemStack arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Item dropItemNaturally(Location arg0, ItemStack arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean generateTree(Location arg0, TreeType arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean generateTree(Location arg0, TreeType arg1,
                    BlockChangeDelegate arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowAnimals() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowMonsters() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Biome getBiome(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getBlockAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getBlockAt(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getBlockTypeIdAt(Location arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Chunk getChunkAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk getChunkAt(Block arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk getChunkAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Difficulty getDifficulty() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1,
                    boolean arg2, boolean arg3) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Entity> getEntities() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T extends Entity> Collection<T> getEntitiesByClass(
                    Class<T>... arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T extends Entity> Collection<T> getEntitiesByClass(
                    Class<T> arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Environment getEnvironment() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getFullTime() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public ChunkGenerator getGenerator() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getHighestBlockAt(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Block getHighestBlockAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getHighestBlockYAt(Location arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getHighestBlockYAt(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public double getHumidity(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean getKeepSpawnInMemory() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<LivingEntity> getLivingEntities() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Chunk[] getLoadedChunks() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMaxHeight() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean getPVP() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<Player> getPlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<BlockPopulator> getPopulators() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getSeaLevel() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getSeed() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Location getSpawnLocation() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public double getTemperature(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getThunderDuration() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTicksPerAnimalSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTicksPerMonsterSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getTime() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public UUID getUID() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getWeatherDuration() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public File getWorldFolder() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public WorldType getWorldType() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasStorm() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isAutoSave() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isChunkLoaded(Chunk arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isChunkLoaded(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isThundering() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void loadChunk(Chunk arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void loadChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean loadChunk(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void playEffect(Location arg0, Effect arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void playEffect(Location arg0, Effect arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T> void playEffect(Location arg0, Effect arg1, T arg2,
                    int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean refreshChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean regenerateChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void save() {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAutoSave(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setDifficulty(Difficulty arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setFullTime(long arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setKeepSpawnInMemory(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setPVP(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setSpawnFlags(boolean arg0, boolean arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setStorm(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setThunderDuration(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setThundering(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTicksPerAnimalSpawns(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTicksPerMonsterSpawns(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTime(long arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setWeatherDuration(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public <T extends Entity> T spawn(Location arg0, Class<T> arg1)
                    throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Arrow spawnArrow(Location arg0, Vector arg1, float arg2,
                    float arg3) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LivingEntity spawnCreature(Location arg0, EntityType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LivingEntity spawnCreature(Location arg0, CreatureType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LightningStrike strikeLightning(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LightningStrike strikeLightningEffect(Location arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean unloadChunk(Chunk arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunk(int arg0, int arg1, boolean arg2,
                    boolean arg3) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunkRequest(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Set<String> getListeningPluginChannels() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public List<MetadataValue> getMetadata(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasMetadata(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void removeMetadata(String arg0, Plugin arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setMetadata(String arg0, MetadataValue arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setBiome(int arg0, int arg1, Biome arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean createExplosion(double arg0, double arg1,
                    double arg2, float arg3, boolean arg4, boolean arg5) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public int getAmbientSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getAnimalSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getGameRuleValue(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getGameRules() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMonsterSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getWaterAnimalSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean isChunkInUse(int arg0, int arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isGameRule(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void playSound(Location arg0, Sound arg1, float arg2,
                    float arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAmbientSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAnimalSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean setGameRuleValue(String arg0, String arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setMonsterSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setWaterAnimalSpawnLimit(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public Entity spawnEntity(Location arg0, EntityType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FallingBlock spawnFallingBlock(Location arg0, Material arg1,
                    byte arg2) throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FallingBlock spawnFallingBlock(Location arg0, int arg1,
                    byte arg2) throws IllegalArgumentException {
                // TODO Auto-generated method stub
                return null;
            }
        };

    }

}
