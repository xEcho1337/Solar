package net.echo.solar.common.boundingbox;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.util.Vector3i;
import net.echo.solar.common.collision.CollisionData;
import net.echo.solar.player.SolarPlayer;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxUtils {

    /**
     * Took from MCP and adapted
     */
    public static List<BoundingBox> getCollidingBoundingBoxes(SolarPlayer player, BoundingBox boundingBox) {
        List<BoundingBox> list = new ArrayList<>();

        int minX = MathUtil.floor(boundingBox.minX());
        int maxX = MathUtil.floor(boundingBox.maxX() + 1.0D);
        int minY = MathUtil.floor(boundingBox.minY());
        int maxY = MathUtil.floor(boundingBox.maxY() + 1.0D);
        int minZ = MathUtil.floor(boundingBox.minZ());
        int maxZ = MathUtil.floor(boundingBox.maxZ() + 1.0D);

        for (int x = minX; x < maxX; ++x)
        {
            for (int z = minZ; z < maxZ; ++z)
            {
                Vector3i blockPosition = new Vector3i(x, 64, z);

                if (player.getWorldTracker().isBlockLoaded(blockPosition))
                {
                    for (int y = minY - 1; y < maxY; ++y)
                    {
                        blockPosition = new Vector3i(x, y, z);

                        WrappedBlockState block = player.getWorldTracker().getBlockAt(blockPosition);

                        if (block == null || !block.getType().isSolid()) continue;

                        BoundingBox blockBoundingBox = CollisionData.getCollisionBox(block, player.getClientVersion()).offset(x, y, z);

                        if (boundingBox.intersectsWith(blockBoundingBox)) {
                            list.add(blockBoundingBox);
                        }
                    }
                }
            }
        }

        // TODO: Entity collision bounding boxes

        return list;
    }
}
