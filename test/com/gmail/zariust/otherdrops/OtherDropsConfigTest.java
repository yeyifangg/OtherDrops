package com.gmail.zariust.otherdrops;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.junit.Test;

import com.avaje.ebean.config.ServerConfig;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.event.CustomDropTest;
import com.gmail.zariust.otherdrops.parameters.Trigger;

public class OtherDropsConfigTest {

    public static final String   TMPFS_DIR = System.getProperty("tmpfs.dir",
                                                   "/tmp/");
    private static final Boolean Boolean   = null;
    static Logger                log       = Logger.getLogger("Minecraft");

    @Test
    public void testDefaultsLoading() throws IOException {
        // Initialization: Bukkit must have a server with a logger.
        Bukkit.setServer(getServer());

        // Create a test drop
        String otherdropsString = "otherdrops:\n" + "  wool@white:\n"
                + "    - tool: dye@red\n"
                + "      replacementblock: wool@red\n"
                + "      consumetool: 1\n";

        // Create the defaults section to test (first testing with no dashes)
        String defaultsString = "defaults:\n" + "  world: TestWorld\n"
                + "  trigger: right_click\n";

        System.out.println("First run....");
        File file = getTempFile(defaultsString + otherdropsString);
        testDefaultsForFile(file);

        // Test defaults as a "section" start with a dash
        defaultsString = "defaults:\n" + "  - world: TestWorld\n"
                + "    trigger: right_click\n";

        System.out.println("Second run....");
        file = getTempFile(defaultsString + otherdropsString);
        testDefaultsForFile(file);

        // Currently no support for defaults with each value dashed, eg:
        defaultsString = "defaults:\n" + "  - world: all\n"
                + "  - trigger: right_click\n";

        // The end :)
    }

    private File getTempFile(String string) throws IOException {
        // Create the temporary directory and file
        File dir = new File(TMPFS_DIR + "/zar_deleteme");
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            System.out.print("tempdir deleted = " + dir.delete() + "/n");
        }

        assertTrue(dir.mkdir());
        System.out.println("Created " + dir.getAbsolutePath());

        File file = new File(dir, "tempfile.yml");

        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(string);
        pw.close();
        fw.close();

        return file;
    }

    void testDefaultsForFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        OtherDropsConfig config = new OtherDropsConfig(new OtherDrops());
        OtherDropsConfig.verbosity = Verbosity.HIGHEST;

        Map<String, Object> map = new HashMap<String, Object>();
        // map.put("trigger", "right_click");

        // ConfigurationNode defaults = new ConfigurationNode(map);
        ConfigurationNode node = null;

        if (yaml.getConfigurationSection("defaults") == null) {
            node = ConfigurationNode.parse(yaml.getMapList("defaults")).get(0);
        } else {
            System.out.println("list: "
                    + yaml.getConfigurationSection("defaults").getKeys(true)
                            .toString());
            ConfigurationSection section = yaml
                    .getConfigurationSection("defaults");
            for (String key : yaml.getConfigurationSection("defaults").getKeys(
                    true)) {
                map.put(key, section.get(key));
            }
            node = new ConfigurationNode(map);
        }

        assertTrue("Node is null!", node != null);

        config.loadModuleDefaults(node);

        System.out.println("Action = " + config.defaultTrigger.toString());
        System.out.println("World = " + config.defaultWorlds.toString());

        assertTrue("Default triggers didn't read?",
                config.defaultTrigger.contains(Trigger.RIGHT_CLICK));
        assertTrue(
                "Default world didn't read?",
                config.defaultWorlds.get(CustomDropTest.testWorld) == java.lang.Boolean.TRUE);
    }

    // get a fake server - only real part is that .getLogger gets an actual
    // logger object
    // also: .getWorld("TestWorld") will return a mock world
    Server getServer() {
        return new Server() {

            @Override
            public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public Set<String> getListeningPluginChannels() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean useExactLoginLocation() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadWorld(World arg0, boolean arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean unloadWorld(String arg0, boolean arg1) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void unbanIP(String arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void shutdown() {
                // TODO Auto-generated method stub

            }

            @Override
            public void setWhitelist(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setSpawnRadius(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setDefaultGameMode(GameMode arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void savePlayers() {
                // TODO Auto-generated method stub

            }

            @Override
            public void resetRecipes() {
                // TODO Auto-generated method stub

            }

            @Override
            public void reloadWhitelist() {
                // TODO Auto-generated method stub

            }

            @Override
            public void reload() {
                // TODO Auto-generated method stub

            }

            @Override
            public Iterator<Recipe> recipeIterator() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Player> matchPlayer(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasWhitelist() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<World> getWorlds() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public File getWorldContainer() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public World getWorld(UUID arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public World getWorld(String arg0) {

                if (arg0.equalsIgnoreCase("TestWorld"))
                    return CustomDropTest.testWorld;
                else if (arg0.equalsIgnoreCase("SecondWorld"))
                    return CustomDropTest.secondWorld;
                return null;
            }

            @Override
            public Set<OfflinePlayer> getWhitelistedPlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getViewDistance() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getVersion() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public File getUpdateFolderFile() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getUpdateFolder() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getTicksPerMonsterSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getTicksPerAnimalSpawns() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getSpawnRadius() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public ServicesManager getServicesManager() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getServerName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getServerId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BukkitScheduler getScheduler() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Recipe> getRecipesFor(ItemStack arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getPort() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public PluginManager getPluginManager() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PluginCommand getPluginCommand(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Player getPlayerExact(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Player getPlayer(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<OfflinePlayer> getOperators() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Player[] getOnlinePlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean getOnlineMode() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public OfflinePlayer[] getOfflinePlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public OfflinePlayer getOfflinePlayer(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Messenger getMessenger() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMaxPlayers() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public MapView getMap(short arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Logger getLogger() {
                // TODO Auto-generated method stub
                return Logger.getLogger("zarTest");
            }

            @Override
            public String getIp() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<String> getIPBans() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public HelpMap getHelpMap() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public GameMode getDefaultGameMode() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ConsoleCommandSender getConsoleSender() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Map<String, String[]> getCommandAliases() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getBukkitVersion() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<OfflinePlayer> getBannedPlayers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean getAllowNether() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowFlight() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean getAllowEnd() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean dispatchCommand(CommandSender arg0, String arg1)
                    throws CommandException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public World createWorld(WorldCreator arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MapView createMap(World arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder arg0, int arg1,
                    String arg2) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder arg0, int arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder arg0,
                    InventoryType arg1) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void configureDbConfig(ServerConfig arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void clearRecipes() {
                // TODO Auto-generated method stub

            }

            @Override
            public int broadcastMessage(String arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int broadcast(String arg0, String arg1) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public void banIP(String arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean addRecipe(Recipe arg0) {
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
            public long getConnectionThrottle() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean getGenerateStructures() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public ItemFactory getItemFactory() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMonsterSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getMotd() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getShutdownMessage() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public WarningState getWarningState() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getWaterAnimalSpawnLimit() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getWorldType() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isHardcore() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isPrimaryThread() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public ScoreboardManager getScoreboardManager() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
