package net.echo.solar.manager;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;

import java.util.concurrent.ThreadLocalRandom;

public class SetBackManager extends AbstractCheck {

    private Vector3d requiredSetback;

    public SetBackManager(SolarPlayer player) {
        super(player);
    }

    public void setback() {
        Vector3d location = player.getPositionTracker().getLastPosition();

        if (requiredSetback == null) {
            requiredSetback = location;
        }

        teleport(requiredSetback);
    }

    public void unblockMovement() {
        setRequiredSetback(null);
    }

    public Vector3d getRequiredSetback() {
        return requiredSetback;
    }

    public void setRequiredSetback(Vector3d requiredSetback) {
        this.requiredSetback = requiredSetback;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (requiredSetback == null) return;

            System.out.println("cancelling cause of setback");
            event.setCancelled(true);
        }
    }

    public void teleport(Vector3d location) {
        int teleportId = ThreadLocalRandom.current().nextInt() | Integer.MIN_VALUE;

        // 24 does not change yaw & pitch, only movement
        player.getUser().sendPacket(new WrapperPlayServerPlayerPositionAndLook(location.getX(), location.getY(), location.getZ(),
                0, 0, (byte) 24, teleportId, false));
    }
}
