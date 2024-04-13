package net.echo.solar.listeners.trackers;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.common.boundingbox.BoundingBox;
import net.echo.solar.player.SolarPlayer;

public class PositionTracker extends AbstractCheck {

    private Vector3d position, lastPosition;
    private float yaw, pitch;
    private float lastYaw, lastPitch;
    private boolean onGround, lastOnGround;

    public PositionTracker(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

            Location location = wrapper.getLocation();

            lastOnGround = onGround;
            onGround = wrapper.isOnGround();

            Vector3d delta = lastPosition == null ? null : lastPosition.subtract(location.getPosition());

            player.getPacketData().setDuplicate(wrapper.hasPositionChanged() && wrapper.hasRotationChanged()
                    && !player.getPacketData().isTeleport()
                    && onGround == lastOnGround
                    && player.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)
                    && delta != null
                    && delta.length() < player.getMoveThreshold());

            if (wrapper.hasPositionChanged() && !player.getPacketData().isDuplicate()) {
                lastPosition = position;
                position = location.getPosition();
            }

            if (wrapper.hasRotationChanged()) {
                lastYaw = yaw;
                lastPitch = pitch;

                yaw = location.getYaw();
                pitch = location.getPitch();
            }

            // Teleports should always have ground status to false in order to get fall damage
            if (player.getPacketData().isTeleport()) {
                wrapper.setOnGround(false);
            }
        }
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3d getLastPosition() {
        return lastPosition;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isLastOnGround() {
        return lastOnGround;
    }

    public Vector3d getMotion() {
        if (position == null || lastPosition == null) return new Vector3d();
        return position.subtract(lastPosition);
    }

    public BoundingBox getBoundingBox(Vector3d position) {
        return BoundingBox.fromPositionAndSize(position, 0.6, 1.8);
    }
}
