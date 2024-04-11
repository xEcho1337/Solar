package net.echo.solar.checks;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import net.echo.solar.Solar;
import net.echo.solar.player.SolarPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class AbstractCheck {

    private final AtomicInteger flags = new AtomicInteger(0);
    private final CheckData checkData;
    protected final SolarPlayer player;

    public AbstractCheck(SolarPlayer player) {
        this.player = player;
        this.checkData = this.getClass().getAnnotation(CheckData.class);
    }

    public void onPacketReceive(PacketReceiveEvent event) {
    }

    public void onPacketSend(PacketSendEvent event) {
    }

    public void flagWithSetback() {
        flagWithSetback("");
    }

    public void flagWithSetback(String verbose) {
        flag(verbose);
        player.getSetBackManager().setback();
    }

    public void flag() {
        flag("");
    }

    public void flag(String verbose) {
        if (checkData == null) {
            throw new NullPointerException("No Check Data provided.");
        }

        int violations = flags.incrementAndGet();

        if (violations % checkData.flagDelay() == 0) {
            Solar.getInstance().getPlayerManager().alertStaff(this, verbose);
        }
    }

    public SolarPlayer getPlayer() {
        return player;
    }

    public int getViolations() {
        return flags.get();
    }

    public CheckData getCheckData() {
        return checkData;
    }
}
