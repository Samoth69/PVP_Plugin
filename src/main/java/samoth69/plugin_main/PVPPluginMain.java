package samoth69.plugin_main;

import com.sun.org.apache.xpath.internal.operations.String;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPPluginMain extends JavaPlugin {

    private FileConfiguration config = getConfig();
    private MyListener listener;

    @Override
    public void onEnable() {
        getLogger().info("Starting");

        Position spawnLocation = new Position(0, 120,0);

        config.addDefault("TeleportOnConnect", true);
        config.addDefault("SpawnCoord", spawnLocation);
        config.options().copyDefaults(true);
        saveDefaultConfig();

        listener = new MyListener(config, this);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling");
    }


}
