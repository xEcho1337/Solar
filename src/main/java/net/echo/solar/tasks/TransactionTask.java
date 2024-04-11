package net.echo.solar.tasks;

import net.echo.solar.Solar;
import net.echo.solar.player.SolarPlayer;

public class TransactionTask implements Runnable {

    private final Solar plugin;

    public TransactionTask(Solar plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (SolarPlayer player : plugin.getPlayerManager().getPlayerList()) {
            long difference = System.currentTimeMillis() - player.getTransactionTracker().getLastSentTransaction();

            if (difference < 120) continue;

            player.sendTransaction(null);
        }
    }
}
