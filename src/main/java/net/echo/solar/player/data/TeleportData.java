package net.echo.solar.player.data;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;

public final class TeleportData {

    private final Vector3d location;
    private final RelativeFlag flags;
    private final float yaw;
    private final float pitch;
    private final int transaction;
    private boolean hasSent, hasReceived;

    public TeleportData(Vector3d location, RelativeFlag flags, float yaw, float pitch, int transaction) {
        this.location = location;
        this.flags = flags;
        this.yaw = yaw;
        this.pitch = pitch;
        this.transaction = transaction;
    }

    public boolean isRelativeX() {
        return flags.isSet(RelativeFlag.X.getMask());
    }

    public boolean isRelativeY() {
        return flags.isSet(RelativeFlag.Y.getMask());
    }

    public boolean isRelativeZ() {
        return flags.isSet(RelativeFlag.Z.getMask());
    }

    public Vector3d getLocation() {
        return location;
    }

    public RelativeFlag getFlags() {
        return flags;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public int getTransaction() {
        return transaction;
    }

    public boolean hasReceived() {
        return hasReceived;
    }

    public void setHasReceived(boolean hasReceived) {
        this.hasReceived = hasReceived;
    }

    public boolean hasSent() {
        return hasSent;
    }

    public void setHasSent(boolean hasSent) {
        this.hasSent = hasSent;
    }
}
