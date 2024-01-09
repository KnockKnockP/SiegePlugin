package knockknockp.siegeplugin;

import knockknockp.siegeplugin.MenKissing.CommandMenKissing;
import knockknockp.siegeplugin.Siege.*;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SiegePlugin extends JavaPlugin {
    private SiegeManager siegeManager;

    @Override
    public void onEnable() {
        siegeManager = new SiegeManager(this);

        Objects.requireNonNull(getCommand("menkissing")).setExecutor(new CommandMenKissing());
        Objects.requireNonNull(getCommand("siege")).setExecutor(new CommandSiege(siegeManager));

        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new ProtectedAreaListener(siegeManager), this);
        pluginManager.registerEvents(new WoolListener(siegeManager), this);
        pluginManager.registerEvents(new ChestListener(siegeManager), this);
        pluginManager.registerEvents(new PlayerListener(siegeManager), this);
        pluginManager.registerEvents(new AssignerListener(siegeManager), this);
        pluginManager.registerEvents(new BlockListener(siegeManager), this);
    }

    @Override
    public void onDisable() {
        siegeManager.stop();
        siegeManager.fullReset();
    }
}