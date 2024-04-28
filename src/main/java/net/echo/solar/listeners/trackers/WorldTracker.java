package net.echo.solar.listeners.trackers;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.Direction;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.ShortArray3d;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_16.Chunk_v1_9;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_7.Chunk_v1_7;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.ListPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.protocol.world.chunk.storage.LegacyFlexibleStorage;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Part;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkDataBulk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WorldTracker extends AbstractCheck {

    private final ConcurrentMap<Long, Column> chunkColumns = new ConcurrentHashMap<>();
    private final TransactionTracker tracker;

    public WorldTracker(SolarPlayer player) {
        super(player);
        this.tracker = player.getTransactionTracker();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

            if (wrapper.getAction() == DiggingAction.FINISHED_DIGGING) {
                Vector3i position = wrapper.getBlockPosition();

                // TODO: Count for blocks like beds
                setBlockAt(position, WrappedBlockState.getDefaultState(StateTypes.AIR));
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            Optional<ItemStack> item = wrapper.getItemStack();

            if (item.isEmpty() || wrapper.getFace() == BlockFace.OTHER) return;

            ItemStack itemStack = item.get();
            Vector3i position = wrapper.getBlockPosition();

            StateType state = itemStack.getType().getPlacedType();

            WrappedBlockState blockAt = getBlockAt(position);

            if (state != null && (blockAt == null || blockAt.getType().isReplaceable())) {
                // TODO: Better handling
                if (ItemTags.BEDS.contains(itemStack.getType())) {
                    int yaw = MathUtil.floor((player.getPositionTracker().getYaw() * 4.0F / 360.0F) + 0.5D) & 3;
                    Direction direction = Direction.getByHorizontalIndex(yaw);

                    BlockFace face = BlockFace.valueOf(direction.name());
                    Vector3i offset = position.offset(face);

                    WrappedBlockState wrapped = WrappedBlockState.getDefaultState(state);
                    wrapped.setPart(Part.HEAD);

                    setBlockAt(offset, wrapped);
                }

                WrappedBlockState blockState = state.createBlockState();
                Vector3i actualPosition = position.offset(wrapper.getFace());

                setBlockAt(actualPosition, blockState);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
            WrapperPlayServerChunkDataBulk wrapper = new WrapperPlayServerChunkDataBulk(event);

            BaseChunk[][] chunks = wrapper.getChunks();

            if (chunks == null) return;

            for (int i = 0; i < wrapper.getChunks().length; i++) {
                int chunkX = wrapper.getX()[i];
                int chunkZ = wrapper.getZ()[i];

                BaseChunk[] subChunks = chunks[i];

                long chunkColumnId = getChunkId(chunkX, chunkZ);

                Column column = new Column(chunkX, chunkZ, true, subChunks, new TileEntity[0]);

                tracker.addNextCallback(callback -> chunkColumns.put(chunkColumnId, column));
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);

            WrappedBlockState state = wrapper.getBlockState();
            Vector3i position = wrapper.getBlockPosition();

            player.sendTransaction(callback -> setBlockAt(position, state));
        }

        if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
            EncodedBlock[] blocks = wrapper.getBlocks();

            player.sendTransaction(callback -> {
                for (EncodedBlock block : blocks) {
                    setBlockAt(block.getX(), block.getY(), block.getZ(), block.getBlockState(player.getClientVersion()));
                }
            });
        }

        if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
            long chunkColumnId = getChunkId(wrapper.getColumn().getX(), wrapper.getColumn().getZ());

            tracker.addNextCallback(callback -> chunkColumns.put(chunkColumnId, wrapper.getColumn()));
        }
    }

    public Column getChunkColumnAt(int chunkX, int chunkZ) {
        long chunkColumnId = getChunkId(chunkX, chunkZ);

        return chunkColumns.get(chunkColumnId);
    }

    public BaseChunk getChunkAt(int chunkX, int chunkY, int chunkZ) {
        Column column = getChunkColumnAt(chunkX, chunkZ);

        if (column == null || chunkY < 0 || chunkY >= column.getChunks().length) return null;

        System.out.println("Chunk at " + chunkX + ", " + chunkY + ", " + chunkZ + " is not null");
        return column.getChunks()[chunkY];
    }

    public WrappedBlockState getBlockAt(int x, int y, int z) {
        BaseChunk chunk = getChunkAt(x >> 4, y >> 4, z >> 4);

        if (chunk == null) {
            chunk = createChunk();
        }

        return chunk.get(player.getClientVersion(), x & 0xF, y & 0xF, z & 0xF);
    }

    public WrappedBlockState getBlockAt(Vector3i position) {
        return getBlockAt(position.getX(), position.getY(), position.getZ());
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return chunkColumns.get(getChunkId(chunkX, chunkZ)) != null;
    }

    public void setBlockAt(int x, int y, int z, WrappedBlockState state) {
        long chunkColumnId = getChunkId(x >> 4, z >> 4);
        Column column = chunkColumns.get(chunkColumnId);

        if (column == null) return;

        BaseChunk chunk = column.getChunks()[y >> 4];

        if (chunk == null) {
            chunk = createChunk();
            chunk.set(player.getClientVersion(), 0, 0, 0, 0);
        }

        chunk.set(x & 0xF, y & 0xF, z & 0xF, state);

        column.getChunks()[y >> 4] = chunk;
        chunkColumns.put(chunkColumnId, column);
    }

    public void setBlockAt(Vector3i position, WrappedBlockState state) {
        setBlockAt(position.getX(), position.getY(), position.getZ(), state);
    }

    private long getChunkId(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    private BaseChunk createChunk() {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18)) {
            return new Chunk_v1_18();
        } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16)) {
            return new Chunk_v1_9(0, DataPalette.createForChunk());
        } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            return new Chunk_v1_9(0, new DataPalette(new ListPalette(4), new LegacyFlexibleStorage(4, 4096), PaletteType.CHUNK));
        } else {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)
                    ? new Chunk_v1_8(new ShortArray3d(4096), null, null)
                    : new Chunk_v1_7(false, true);
        }
    }

    public boolean isBlockLoaded(Vector3i blockPosition) {
        return getBlockAt(blockPosition) != null;
    }
}
