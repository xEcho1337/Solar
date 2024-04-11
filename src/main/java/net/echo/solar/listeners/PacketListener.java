package net.echo.solar.listeners;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import net.echo.solar.Solar;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;

public class PacketListener extends PacketListenerAbstract {

    private final Solar plugin;

    public PacketListener(Solar plugin) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        SolarPlayer player = plugin.getPlayerManager().fromCache(event.getUser());

        if (player == null) return;

        for (AbstractCheck tracker : player.getCheckManager().getTrackers()) {
            tracker.onPacketReceive(event);
        }

        for (AbstractCheck check : player.getCheckManager().getChecks()) {
            check.onPacketReceive(event);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            plugin.getPlayerManager().addPlayer(event.getUser());
        }

        SolarPlayer player = plugin.getPlayerManager().fromCache(event.getUser());

        if (player == null) return;

        for (AbstractCheck tracker : player.getCheckManager().getTrackers()) {
            tracker.onPacketSend(event);
        }

        for (AbstractCheck check : player.getCheckManager().getChecks()) {
            check.onPacketSend(event);
        }
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        plugin.getPlayerManager().removePlayer(event.getUser());
        super.onUserDisconnect(event);
    }
}
