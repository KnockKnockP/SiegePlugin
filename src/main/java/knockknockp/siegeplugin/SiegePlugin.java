package knockknockp.siegeplugin;

import knockknockp.siegeplugin.MenKissing.CommandMenKissing;
import knockknockp.siegeplugin.Siege.CommandSiege;
import knockknockp.siegeplugin.Siege.ProtectedAreaListener;
import knockknockp.siegeplugin.Siege.SiegeManager;
import knockknockp.siegeplugin.Siege.WoolListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SiegePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        SiegeManager siegeManager = new SiegeManager();
        getCommand("menkissing").setExecutor(new CommandMenKissing());
        getCommand("siege").setExecutor(new CommandSiege(siegeManager));
        getServer().getPluginManager().registerEvents(new ProtectedAreaListener(siegeManager), this);
        getServer().getPluginManager().registerEvents(new WoolListener(siegeManager), this);
    }
}