package net.echo.solar.player.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class TransactionData {

    private List<Consumer<Integer>> callbacks = new ArrayList<>();
    private final int transaction;
    private final long timestamp;

    public TransactionData(int transaction, long timestamp) {
        this.transaction = transaction;
        this.timestamp = timestamp;
    }

    public int transaction() {
        return transaction;
    }

    public long timestamp() {
        return timestamp;
    }

    public void setCallbacks(List<Consumer<Integer>> callbacks) {
        this.callbacks = callbacks;
    }

    public List<Consumer<Integer>> getCallbacks() {
        return callbacks;
    }
}
