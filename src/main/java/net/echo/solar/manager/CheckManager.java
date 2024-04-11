package net.echo.solar.manager;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.impl.misc.OnePointSeventeenDuplicateFix;
import net.echo.solar.checks.impl.misc.fastbreak.LegacyFastBreak;
import net.echo.solar.checks.impl.misc.order.TeleportOrder;
import net.echo.solar.checks.impl.misc.order.TransactionOrder;
import net.echo.solar.checks.impl.movement.TickSpeed;
import net.echo.solar.listeners.trackers.PositionTracker;
import net.echo.solar.listeners.trackers.TeleportTracker;
import net.echo.solar.listeners.trackers.TransactionTracker;
import net.echo.solar.listeners.trackers.WorldTracker;
import net.echo.solar.player.SolarPlayer;

import java.util.Collection;

public class CheckManager {

    private final ClassToInstanceMap<AbstractCheck> checks;
    private final ClassToInstanceMap<AbstractCheck> trackers;

    public CheckManager(SolarPlayer player) {
        this.checks = new ImmutableClassToInstanceMap.Builder<AbstractCheck>()
                .put(TransactionOrder.class, new TransactionOrder(player))
                .put(TeleportOrder.class, new TeleportOrder(player))
                .put(TickSpeed.class, new TickSpeed(player))
                .put(LegacyFastBreak.class, new LegacyFastBreak(player))
                .put(OnePointSeventeenDuplicateFix.class, new OnePointSeventeenDuplicateFix(player))
                .build();

        this.trackers = new ImmutableClassToInstanceMap.Builder<AbstractCheck>()
                .put(WorldTracker.class, new WorldTracker(player))
                .put(SetBackManager.class, new SetBackManager(player))
                .put(TeleportTracker.class, new TeleportTracker(player))
                .put(PositionTracker.class, new PositionTracker(player))
                .put(TransactionTracker.class, new TransactionTracker(player))
                .build();
    }

    public Collection<AbstractCheck> getChecks() {
        return checks.values();
    }

    public Collection<AbstractCheck> getTrackers() {
        return trackers.values();
    }

    public <T extends AbstractCheck> T getCheck(Class<T> checkClass) {
        return (T) checks.get(checkClass);
    }

    public <T extends AbstractCheck> T getTracker(Class<T> trackerClass) {
        return (T) trackers.get(trackerClass);
    }
}