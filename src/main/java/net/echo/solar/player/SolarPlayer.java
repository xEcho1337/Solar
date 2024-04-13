package net.echo.solar.player;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import net.echo.solar.common.boundingbox.BoundingBox;
import net.echo.solar.listeners.trackers.PositionTracker;
import net.echo.solar.listeners.trackers.TeleportTracker;
import net.echo.solar.listeners.trackers.TransactionTracker;
import net.echo.solar.listeners.trackers.WorldTracker;
import net.echo.solar.manager.CheckManager;
import net.echo.solar.manager.SetBackManager;
import net.echo.solar.player.data.PacketData;
import net.echo.solar.predictions.engine.PredictionEngine;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class SolarPlayer {

    private final SetBackManager setBackManager = new SetBackManager(this);
    private final PositionTracker positionTracker = new PositionTracker(this);
    private final TeleportTracker teleportTracker = new TeleportTracker(this);
    private final TransactionTracker transactionTracker = new TransactionTracker(this);
    private final WorldTracker worldTracker = new WorldTracker(this);
    private final CheckManager checkManager = new CheckManager(this);
    private final PredictionEngine predictionEngine = new PredictionEngine(this);
    private final PacketData packetData = new PacketData();
    private final User user;
    private Player bukkitPlayer;

    public SolarPlayer(User user) {
        this.user = user;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public PacketData getPacketData() {
        return packetData;
    }

    public PredictionEngine getPredictionEngine() {
        return predictionEngine;
    }

    public TransactionTracker getTransactionTracker() {
        return transactionTracker;
    }

    public SetBackManager getSetBackManager() {
        return setBackManager;
    }

    public TeleportTracker getTeleportTracker() {
        return teleportTracker;
    }

    public PositionTracker getPositionTracker() {
        return positionTracker;
    }

    public WorldTracker getWorldTracker() {
        return worldTracker;
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

    public BoundingBox getBoundingBox(Vector3d position) {
        return getPositionTracker().getBoundingBox(position);
    }
}
