package net.echo.solar.common.movement;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import net.echo.solar.listeners.trackers.PositionTracker;
import net.echo.solar.player.SolarPlayer;

public class ConstantsCalculator {

    public static float getFrictionFactor(SolarPlayer player, boolean onGround) {
        float friction = 0.91f;

        // TODO: Friction based on blocks actual slipperness
        if (onGround) {
            friction *= getGroundFriction(player);
        }

        return friction;
    }

    private static float getGroundFriction(SolarPlayer player) {
        PositionTracker tracker = player.getPositionTracker();

        Vector3i position = tracker.getLastPosition().subtract(0, 1, 0).toVector3i();
        WrappedBlockState block = player.getWorldTracker().getBlockAt(position);

        if (block == null) return 0.6f;

        return getBlockFriction(player, block.getType());
    }

    private static float getBlockFriction(SolarPlayer player, StateType type) {
        float friction = 0.6f;

        // Credits to Grim
        if (type == StateTypes.ICE) friction = 0.98f;
        if (type == StateTypes.SLIME_BLOCK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) friction = 0.8f;
        if (type == StateTypes.HONEY_BLOCK && player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) friction = 0.8f;
        if (type == StateTypes.PACKED_ICE || type == StateTypes.FROSTED_ICE) friction = 0.98f;
        if (type == StateTypes.BLUE_ICE) friction = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13) ? 0.989f : 0.98f;

        return friction;
    }
}
