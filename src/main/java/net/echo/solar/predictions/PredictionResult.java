package net.echo.solar.predictions;

import com.github.retrooper.packetevents.util.Vector3d;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.predictions.possibility.MovePossibility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PredictionResult {

    private final List<MovePossibility> movePossibilities = new ArrayList<>();
    private final Vector3d originalVelocity;
    private final SolarPlayer player;

    public PredictionResult(SolarPlayer player, Vector3d originalVelocity) {
        this.player = player;
        this.originalVelocity = originalVelocity;
    }

    public double getOffset() {
        return originalVelocity.distance(getClosestPossibility().getMotion());
    }

    public MovePossibility getClosestPossibility() {
        return movePossibilities.stream()
                .min(Comparator.comparingDouble(move -> move.getMotion().distanceSquared(originalVelocity)))
                .orElse(new MovePossibility(player));
    }

    public void addMovePossibility(MovePossibility possibility) {
        movePossibilities.add(possibility);
    }

    public List<MovePossibility> getMovePossibilities() {
        return movePossibilities;
    }
}
