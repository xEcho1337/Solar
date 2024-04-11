package net.echo.solar.player;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import net.echo.solar.listeners.trackers.PositionTracker;
import net.echo.solar.listeners.trackers.TeleportTracker;
import net.echo.solar.listeners.trackers.TransactionTracker;
import net.echo.solar.listeners.trackers.WorldTracker;
import net.echo.solar.manager.CheckManager;
import net.echo.solar.manager.SetBackManager;
import net.echo.solar.player.data.PacketData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class SolarPlayer {

    private final CheckManager checkManager = new CheckManager(this);
    private final PacketData packetData = new PacketData();
    private final User user;
    private Player bukkitPlayer;

    public SolarPlayer(User user) {
        this.user = user;
    }

    public SetBackManager getSetBackManager() {
        return checkManager.getTracker(SetBackManager.class);
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public PacketData getPacketData() {
        return packetData;
    }

    public WorldTracker getWorldTracker() {
        return checkManager.getTracker(WorldTracker.class);
    }

    public PositionTracker getPositionTracker() {
        return checkManager.getTracker(PositionTracker.class);
    }

    public TransactionTracker getTransactionTracker() {
        return checkManager.getTracker(TransactionTracker.class);
    }

    public TeleportTracker getTeleportTracker() {
        return checkManager.getTracker(TeleportTracker.class);
    }

    public User getUser() {
        return user;
    }

    public double getMoveThreshold() {
        return user.getClientVersion().isOlderThan(ClientVersion.V_1_18_2) ? 0.03 : 0.002;
    }

    public ClientVersion getClientVersion() {
        return user.getClientVersion();
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public void sendTransaction(Consumer<Integer> callback) {
        getTransactionTracker().sendTransaction(callback);
    }

    public void kickPlayer(String reason) {
        Component component = Component.text(ChatColor.translateAlternateColorCodes('&', reason));
        WrapperPlayServerDisconnect disconnect = new WrapperPlayServerDisconnect(component);

        user.sendPacket(disconnect);
        user.closeConnection();
    }
}
