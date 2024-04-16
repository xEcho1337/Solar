package net.echo.solar.checks.impl.movement;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.player.data.TransactionData;

@CheckData(name = "TickSpeed", description = "Checks abnormalities in the game tick speed", flagDelay = 10)
public class TickSpeed extends AbstractCheck {

    private long nanosBalance = (long) (System.nanoTime() - 120e6);

    public TickSpeed(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (player.getPacketData().isTeleport() || player.getPacketData().isDuplicate()) return;

            nanosBalance += (long) 50e6;

            if (nanosBalance > System.nanoTime()) {
                flagWithSetback("Difference: " + (nanosBalance - System.nanoTime()) / 1e9);

                nanosBalance -= (long) 50e6;

                event.setCancelled(true);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);
            int id = wrapper.getActionId();

            handleTransaction(id);
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong wrapper = new WrapperPlayClientPong(event);
            int id = wrapper.getId();

            handleTransaction(id);
        }
    }

    public void handleTransaction(int id) {
        TransactionData data = player.getTransactionTracker().getDataFromId(id);

        if (data == null) return;

        // Without this, 1.9+ players could use timer whenever they wanted - xEcho1337
        nanosBalance = (long) Math.max(nanosBalance, data.timestamp() - 120e6);
    }
}
