package net.echo.solar.checks.impl.misc.order;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.player.SolarPlayer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@CheckData(name = "Transaction Order", description = "Checks the order of the transactions")
public class TransactionOrder extends AbstractCheck {

    private final Queue<Integer> transactionQueue = new ConcurrentLinkedQueue<>();

    public TransactionOrder(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);
            handleCheck(wrapper.getActionId());
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong wrapper = new WrapperPlayClientPong(event);
            handleCheck(wrapper.getId());
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation wrapper = new WrapperPlayServerWindowConfirmation(event);
            transactionQueue.add((int) wrapper.getActionId());
        }

        if (event.getPacketType() == PacketType.Play.Server.PING) {
            WrapperPlayServerPing wrapper = new WrapperPlayServerPing(event);
            transactionQueue.add(wrapper.getId());
        }
    }

    public void handleCheck(int id) {
        if (transactionQueue.isEmpty()) return;

        int next = transactionQueue.poll();

        if (next != id) {
            flag("Expected: " + next + " Received: " + id);
            player.kickPlayer("&cSi è verificato un errore nella tua connessione.");
        }

        transactionQueue.remove(id);
    }
}
