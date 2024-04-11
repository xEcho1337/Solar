package net.echo.solar.checks.impl.misc;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.player.SolarPlayer;

@CheckData(name = "1.17 Duplicate Fix", description = "Prevents 1.17 duplicates packets from being received by the server")
public class OnePointSeventeenDuplicateFix extends AbstractCheck {

    public OnePointSeventeenDuplicateFix(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;

        if (!player.getPacketData().isExemptIs117Duplicate()) return;

        event.setCancelled(true);
    }
}
