package net.echo.solar.manager;

import com.github.retrooper.packetevents.protocol.player.User;
import net.echo.solar.Solar;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerManager {

    private final Solar plugin;
    private final ConcurrentMap<UUID, SolarPlayer> playerList = new ConcurrentHashMap<>();

    public PlayerManager(Solar plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(User user) {
        SolarPlayer data = new SolarPlayer(user);

        if (user == null) {
            System.out.println("[Warning] User is null");
            return;
        }

        if (user.getUUID() == null) {
            System.out.println("[Warning] UUID is null");
        }

        playerList.put(user.getUUID(), data);
    }

    public void removePlayer(User user) {
        if (user == null) return;
        if (user.getUUID() == null) return;

        playerList.remove(user.getUUID());
    }

    public void alertStaff(AbstractCheck check, String verbose) {
        String message = plugin.getConfig().getString("alert-message")
                .replaceAll("%player%", check.getPlayer().getUser().getName())
                .replaceAll("%vl%", String.valueOf(check.getViolations()))
                .replaceAll("%check%", check.getCheckData().name())
                .replaceAll("%verbose%", verbose);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("solar.alerts")) continue;

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public Collection<SolarPlayer> getPlayerList() {
        return playerList.values();
    }

    public SolarPlayer fromCache(User user) {
        if (user == null) return null;
        if (user.getUUID() == null) return null;

        return playerList.getOrDefault(user.getUUID(), null);
    }
}
