package net.echo.solar;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.echo.solar.listeners.PacketListener;
import net.echo.solar.listeners.bukkit.JoinListener;
import net.echo.solar.manager.PlayerManager;
import net.echo.solar.tasks.TransactionTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Solar extends JavaPlugin {

    private static Solar instance;
    private final PlayerManager playerManager = new PlayerManager(this);

    @Override
    public void onLoad() {
        instance = this;

        saveDefaultConfig();

        PacketEventsAPI<?> api = SpigotPacketEventsBuilder.build(this);

        PacketEvents.setAPI(api);

        api.getSettings().bStats(false).checkForUpdates(false).debug(false);
        api.load();

        api.getEventManager().registerListener(new PacketListener(this));
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TransactionTask(this), 1L, 1L);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static Solar getInstance() {
        return instance;
    }
}
