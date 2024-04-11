package net.echo.solar.listeners.trackers;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.impl.misc.order.TeleportOrder;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.player.data.TeleportData;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TeleportTracker extends AbstractCheck {

    private final Queue<TeleportData> teleportQueue = new ConcurrentLinkedQueue<>();

    public TeleportTracker(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);

            player.sendTransaction(callback -> teleportQueue.add(new TeleportData(new Vector3d(wrapper.getX(), wrapper.getY(),
                    wrapper.getZ()), wrapper.getRelativeFlags(), wrapper.getYaw(), wrapper.getPitch(), callback)));
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            player.getPacketData().setExemptIsTeleport(false);

            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

            if (!wrapper.hasRotationChanged() || !wrapper.hasPositionChanged()) return;

            Vector3d lastPosition = player.getPositionTracker().getLastPosition();

            if (lastPosition == null) return;

            Vector3d position = wrapper.getLocation().getPosition();
            Vector3d requiredSetback = player.getSetBackManager().getRequiredSetback();

            if (teleportQueue.isEmpty()) {
                if (requiredSetback != null) player.getSetBackManager().teleport(requiredSetback);

                return;
            }

            TeleportData data = teleportQueue.poll();

            if (data == null) return;

            Vector3d expected = data.getLocation();

            double actualX = (data.isRelativeX() ? lastPosition.getX() : 0) + expected.getX();
            double actualY = (data.isRelativeY() ? lastPosition.getY() : 0) + expected.getY();
            double actualZ = (data.isRelativeZ() ? lastPosition.getZ() : 0) + expected.getZ();

            if (position.equals(new Vector3d(actualX, actualY, actualZ))) {
                player.getPacketData().setExemptIsTeleport(true);

                if (requiredSetback != null && requiredSetback.equals(position)) {
                    player.getSetBackManager().unblockMovement();
                }
            } else if (data.getTransaction() > player.getTransactionTracker().getLastReceivedTransaction().get()) {
                TeleportOrder order = player.getCheckManager().getCheck(TeleportOrder.class);

                order.flag();
            }
        }
    }

    public void addToQueue(TeleportData data) {
        teleportQueue.add(data);
    }
}
