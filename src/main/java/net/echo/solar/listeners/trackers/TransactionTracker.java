package net.echo.solar.listeners.trackers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.player.data.TransactionData;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TransactionTracker extends AbstractCheck {

    // Idk maybe do something better for the callbacks
    private final AtomicInteger currentTransaction = new AtomicInteger(0);
    private final AtomicInteger lastReceivedTransaction = new AtomicInteger(0);
    private final Map<Integer, List<Consumer<Integer>>> callbacks = new ConcurrentHashMap<>();
    private final Map<Integer, TransactionData> sentTransactions = new ConcurrentHashMap<>();
    private long lastSentTransaction = System.currentTimeMillis();

    public TransactionTracker(SolarPlayer player) {
        super(player);
    }

    public void addCallback(int transaction, Consumer<Integer> callback) {
        List<Consumer<Integer>> tasks = callbacks.getOrDefault(transaction, new ArrayList<>());

        tasks.add(callback);

        callbacks.put(transaction, tasks);
    }

    public void addNextCallback(Consumer<Integer> callback) {
        addCallback(currentTransaction.get() + 1, callback);
    }

    public TransactionData getDataFromId(int transaction) {
        return sentTransactions.get(transaction);
    }

    public void sendTransaction(Consumer<Integer> callback) {
        int transaction = currentTransaction.decrementAndGet();

        TransactionData data = new TransactionData(transaction, System.currentTimeMillis());
        List<Consumer<Integer>> tasks = new ArrayList<>();

        if (callback != null) tasks.add(callback);

        data.setCallbacks(tasks);

        callbacks.put(transaction, tasks);
        sentTransactions.put(transaction, data);

        PacketWrapper<?> wrapper = new WrapperPlayServerWindowConfirmation(0, (short) transaction, false);

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            wrapper =  new WrapperPlayServerPing(transaction);
        }

        player.getUser().sendPacket(wrapper);

        lastSentTransaction = System.currentTimeMillis();

        callbacks.forEach((id, consumers) -> {
            if (id > transaction) {
                consumers.forEach(consumer -> consumer.accept(id));
            }
        });

        callbacks.entrySet().removeIf(entry -> entry.getKey() > transaction);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);

            handleTransaction(wrapper.getActionId());
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong wrapper = new WrapperPlayClientPong(event);

            handleTransaction(wrapper.getId());
        }
    }

    public void handleTransaction(int id) {
        TransactionData data = getDataFromId(id);

        if (data == null) return;

        lastReceivedTransaction.set(id);

        if (callbacks.containsKey(id)) {
            callbacks.get(id).forEach(callback -> callback.accept(data.transaction()));
            callbacks.remove(id);
        }

        sentTransactions.remove(id);
    }

    public AtomicInteger getLastReceivedTransaction() {
        return lastReceivedTransaction;
    }

    public long getLastSentTransaction() {
        return lastSentTransaction;
    }
}
