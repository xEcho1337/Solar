package net.echo.solar.common.collision;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import net.echo.solar.common.boundingbox.BoundingBox;

public interface CollisionFactory {

    BoundingBox check(WrappedBlockState state, ClientVersion version);
}
