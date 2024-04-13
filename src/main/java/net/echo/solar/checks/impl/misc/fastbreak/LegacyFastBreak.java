package net.echo.solar.checks.impl.misc.fastbreak;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.common.BreakUtils;
import net.echo.solar.player.SolarPlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

@CheckData(name = "Fast Break (Legacy)", description = "Checks for faster block breaking.")
public class LegacyFastBreak extends AbstractCheck {

    private Vector3i targetBlock;
    private float balance;

    public LegacyFastBreak(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isNewerThan(ClientVersion.V_1_8)) return;

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (player.getPacketData().isTeleport() || targetBlock == null) return;

            balance += BreakUtils.getBlockDamage(player, targetBlock);
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

            switch (wrapper.getAction()) {
                case START_DIGGING -> {
                    targetBlock = wrapper.getBlockPosition();

                    float damage = BreakUtils.getBlockDamage(player, targetBlock);

                    if (damage == 0) {
                        targetBlock = null;
                        return;
                    }

                    balance = damage;
                }
                case FINISHED_DIGGING -> {
                    // Prevents a fast break disabler
                    if (balance == 0 || targetBlock == null) {
                        event.setCancelled(true);
                        updateBlock(wrapper.getBlockPosition());
                        return;
                    }

                    if (balance < 1) {
                        flag("Balance: " + balance);

                        event.setCancelled(true);
                        updateBlock(wrapper.getBlockPosition());
                    }

                    balance = 0;
                    targetBlock = null;
                }
                case CANCELLED_DIGGING -> {
                    balance = 0;
                    targetBlock = null;
                }
            }
        }
    }

    public void updateBlock(Vector3i position) {
        World world = player.getBukkitPlayer().getWorld();

        Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
        int blockId = (block.getType().getId() << 4) | block.getData();

        WrapperPlayServerBlockChange change = new WrapperPlayServerBlockChange(position, blockId);
        player.getUser().sendPacket(change);
    }
}
