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

            /* Debug shit
            Player user = player.getBukkitPlayer();
            MovePossibility move = result.getClosestPossibility();

            if (user != null) {
                user.sendMessage("-- Prediction result! --");
                user.sendMessage("Position: " + wrapper.hasPositionChanged());
                user.sendMessage("C velocity: " + player.getPositionTracker().getMotion());
                user.sendMessage("P velocity: " + move.getMotion());
                user.sendMessage("Client on ground: " + wrapper.isOnGround());
                user.sendMessage("Server on ground: " + move.isOnGround());
                user.sendMessage("Difference: " + result.getOffset());
                user.sendMessage("Forward: " + move.getForward());
                user.sendMessage("Strafe: " + move.getStrafe());
            }*/

            // TODO: 0.03 handling, add a secondary position tracker using the motion from predictions
            if (!wrapper.hasPositionChanged()) return;

            double offset = result.getOffset();

            if (offset < 0.0001) return;

            flag("Offset: " + offset);
        }
    }
}
