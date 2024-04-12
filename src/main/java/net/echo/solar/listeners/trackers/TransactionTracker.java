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
    private final Map<TransactionData, List<Consumer<Integer>>> callbacks = new ConcurrentHashMap<>();
    private long lastSentTransaction = System.currentTimeMillis();

    public TransactionTracker(SolarPlayer player) {
        super(player);
    }

    public void addCallback(int transaction, Consumer<Integer> callback) {
        TransactionData data = getDataFromId(transaction);

        List<Consumer<Integer>> tasks = callbacks.getOrDefault(data, new ArrayList<>());

        tasks.add(callback);

        callbacks.put(data, tasks);
    }

    public void addNextCallback(Consumer<Integer> callback) {
        addCallback(currentTransaction.get() + 1, callback);
    }

    public TransactionData getDataFromId(int transaction) {
        for (TransactionData data : callbacks.keySet()) {
            if (data.transaction() != transaction) continue;

            return data;
        }

        return null;
    }

    public void sendTransaction(Consumer<Integer> callback) {
        int transaction = currentTransaction.decrementAndGet();

        TransactionData data = new TransactionData(transaction, System.currentTimeMillis());
        List<Consumer<Integer>> tasks = new ArrayList<>();

        if (callback != null) tasks.add(callback);

        callbacks.put(data, tasks);

        PacketWrapper<?> wrapper = new WrapperPlayServerWindowConfirmation(0, (short) transaction, false);

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            wrapper =  new WrapperPlayServerPing(transaction);
        }

        player.getUser().sendPacket(wrapper);

        lastSentTransaction = System.currentTimeMillis();

        callbacks.forEach((transactionData, consumers) -> {
            int id = transactionData.transaction();

            if (id > transaction) {
                consumers.forEach(consumer -> consumer.accept(id));
            }
        });

        callbacks.entrySet().removeIf(entry -> entry.getKey().transaction() > transaction);

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

        callbacks.get(data).forEach(callback -> callback.accept(data.transaction()));
        callbacks.remove(data);
    }

    public AtomicInteger getLastReceivedTransaction() {
        return lastReceivedTransaction;
    }

    public long getLastSentTransaction() {
        return lastSentTransaction;
    }
}
