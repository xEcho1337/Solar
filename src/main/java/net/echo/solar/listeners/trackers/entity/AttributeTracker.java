package net.echo.solar.listeners.trackers.entity;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes.*;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;

public class AttributeTracker extends AbstractCheck {

    private double movementSpeed = 0.10000000149011612D;
    private double knockbackResistance = 0;
    private boolean sprinting;

    public AttributeTracker(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);

            if (wrapper.getAction() == Action.START_SPRINTING) {
                sprinting = true;
            }

            if (wrapper.getAction() == Action.STOP_SPRINTING) {
                sprinting = false;
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);

            TrackedEntity entity = player.getEntityTracker().getSelf();

            if (wrapper.getEntityId() != entity.getEntityId()) return;

            event.getTasksAfterSend().add(() -> player.sendTransaction(callback -> {
                for (Property property : wrapper.getProperties()) {
                    entity.setProperty(property);
                }

                for (Property property : entity.getProperties()) {
                    if (!property.getKey().toLowerCase().contains("movement")) continue;

                    movementSpeed = property.getValue();

                    break;
                }
            }));
        }
    }

    public double getMovementSpeed() {
        return sprinting ? movementSpeed * 1.3000001 : movementSpeed;
    }

    public double getKnockbackResistance() {
        return knockbackResistance;
    }

    public boolean isSprinting() {
        return sprinting;
    }
}
