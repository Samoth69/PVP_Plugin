package samoth69.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPPluginMain extends JavaPlugin {

    private FileConfiguration config = getConfig();
    private Main listener;

    @Override
    public void onEnable() {
        getLogger().info("Starting");

        Position spawnLocation = new Position(0, 120,0);

        config.addDefault("TeleportOnConnect", true);
        config.addDefault("SpawnCoord", spawnLocation);
        config.options().copyDefaults(true);
        saveDefaultConfig();

        listener = new Main(config, this);
        getServer().getPluginManager().registerEvents(listener, this);
        this.getCommand("pvp").setExecutor(listener);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling");
    }


}
