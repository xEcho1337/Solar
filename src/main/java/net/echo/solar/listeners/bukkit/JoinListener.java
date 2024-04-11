package net.echo.solar.listeners.bukkit;

import net.echo.solar.Solar;
import net.echo.solar.player.SolarPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final Solar plugin;

    public JoinListener(Solar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (SolarPlayer player : plugin.getPlayerManager().getPlayerList()) {
            if (!player.getUser().getUUID().equals(event.getPlayer().getUniqueId())) continue;

            player.setBukkitPlayer(event.getPlayer());
        }
    }
}
