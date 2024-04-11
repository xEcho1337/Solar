package net.echo.solar.checks.impl.misc.order;

import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.player.SolarPlayer;

@CheckData(name = "Teleport Order", description = "Checks the order of the teleports", flagDelay = 10)
public class TeleportOrder extends AbstractCheck {

    public TeleportOrder(SolarPlayer player) {
        super(player);
    }
}
