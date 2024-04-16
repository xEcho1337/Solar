package net.echo.solar.checks.impl.movement.simulation;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import net.echo.solar.checks.AbstractCheck;
import net.echo.solar.checks.CheckData;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.predictions.PredictionResult;

@CheckData(name = "Prediction", description = "Predicts all the possible player movements")
public class Prediction extends AbstractCheck {

    public Prediction(SolarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (player.getPacketData().isTeleport() || player.getPacketData().isDuplicate()) return;

            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            PredictionResult result = player.getPredictionEngine().getResult();

            // TODO: 0.03 handling, add a secondary position tracker using the motion from predictions - xEcho1337
            if (!wrapper.hasPositionChanged()) return;

            System.out.println(player.getPositionTracker().getMotion());
            System.out.println(result.getClosestPossibility().getMotion());
            System.out.print(result.getOffset());

            double offset = result.getOffset();

            if (offset < 0.0001) return;

            flag("Offset: " + offset);
        }
    }
}
