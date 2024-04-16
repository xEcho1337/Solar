package net.echo.solar.listeners.trackers.entity;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends AbstractCheck {

    private final Map<Integer, TrackedEntity> trackedEntities = new ConcurrentHashMap<>();
    private TrackedEntity self;

    public EntityTracker(SolarPlayer player) {
        super(player);
    }

    public TrackedEntity getSelf() {
        return self;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);

            self = new TrackedEntity(wrapper.getEntityId());
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);

            int entityId = wrapper.getEntityId();

            if (!trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = new TrackedEntity(entityId);
                    trackedEntity.setPosition(wrapper.getPosition());

                    trackedEntities.put(entityId, trackedEntity);
                });
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_LIVING_ENTITY) {
            WrapperPlayServerSpawnLivingEntity wrapper = new WrapperPlayServerSpawnLivingEntity(event);

            int entityId = wrapper.getEntityId();

            if (!trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = new TrackedEntity(entityId);
                    trackedEntity.setPosition(wrapper.getPosition());

                    trackedEntities.put(entityId, trackedEntity);
                });
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(event);

            int entityId = wrapper.getEntityId();

            if (!trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = new TrackedEntity(entityId);
                    trackedEntity.setPosition(wrapper.getPosition());

                    trackedEntities.put(entityId, trackedEntity);
                });
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);

            int entityId = wrapper.getEntityId();

            if (trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = trackedEntities.get(entityId);

                    trackedEntity.move(wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
                });
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);

            int entityId = wrapper.getEntityId();

            if (trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = trackedEntities.get(entityId);

                    trackedEntity.setPosition(wrapper.getPosition());
                });
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);

            int entityId = wrapper.getEntityId();

            if (trackedEntities.containsKey(entityId)) {
                player.getTransactionTracker().addNextCallback(callback -> {
                    TrackedEntity trackedEntity = trackedEntities.get(entityId);

                    trackedEntity.move(wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
                });
            }
        }
    }
}
