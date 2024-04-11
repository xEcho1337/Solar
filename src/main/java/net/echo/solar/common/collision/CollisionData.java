package net.echo.solar.common.collision;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Axis;
import com.github.retrooper.packetevents.protocol.world.states.enums.Type;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import net.echo.solar.common.boundingbox.BoundingBox;

import java.util.HashMap;
import java.util.Map;

public enum CollisionData {

    DEFAULT((state, version) -> BoundingBox.fromBounds(0, 0, 0, 1, 1, 1)),
    SNOW((state, version) -> BoundingBox.fromBounds(0, 0, 0, 1, (state.getLayers() - 1) * 0.125, 1), StateTypes.SNOW),
    ENDER_CHEST((state, version) -> BoundingBox.fromBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F), StateTypes.ENDER_CHEST),
    CHEST((state, version) -> BoundingBox.fromBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F), StateTypes.CHEST),
    SLAB((state, version) -> {
        Type type = state.getTypeData();

        if (type == Type.DOUBLE) {
            return BoundingBox.fromBounds(0, 0, 0, 1, 1, 1);
        }

        return BoundingBox.fromBounds(0, 0, 0, 1, 0.5, 1);
    }, BlockTags.SLABS.getStates().toArray(new StateType[0])),
    BED((state, version) -> BoundingBox.fromBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625, 1.0F)),
    ENCHANTING_TABLE((state, version) -> BoundingBox.fromBounds(0, 0, 0, 1, 1 - 0.25, 1), StateTypes.ENCHANTING_TABLE),
    ANVIL((state, version) -> {
        Axis axis = state.getAxis();

        if (axis == Axis.X) {
            return BoundingBox.fromBounds(0.0F, 0.0F, 0.125F, 1.0F, 1.0F, 0.875F);
        } else {
            return BoundingBox.fromBounds(0.125F, 0.0F, 0.0F, 0.875F, 1.0F, 1.0F);
        }
    }, BlockTags.ANVIL.getStates().toArray(new StateType[0]));

    private static final Map<StateType, CollisionData> collisionData = new HashMap<>();
    private final CollisionFactory factory;
    private final StateType[] types;

    static {
        for (CollisionData data : values()) {
            // Support for multiple versions of the blocks
            for (StateType type : data.types) {
                collisionData.put(type, data);
            }
        }
    }

    CollisionData(CollisionFactory factory, StateType... states) {
        this.types = states;
        this.factory = factory;
    }

    public CollisionFactory factory() {
        return factory;
    }

    public static BoundingBox getCollisionBox(WrappedBlockState state, ClientVersion version) {
        if (state == null) return new BoundingBox(new Vector3d(), new Vector3d());

        return collisionData.getOrDefault(state.getType(), DEFAULT).factory().check(state, version);
    }
}
