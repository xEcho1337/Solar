package net.echo.solar.predictions.engine;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import net.echo.solar.listeners.trackers.position.PositionTracker;
import net.echo.solar.player.SolarPlayer;
import net.echo.solar.predictions.possibility.MovePossibility;
import net.echo.solar.predictions.PredictionResult;
import net.echo.solar.predictions.possibility.PossibilityType;

public class PredictionEngine {

    private final PositionTracker tracker;
    private final SolarPlayer player;
    private MovePossibility lastValidMovement;

    public PredictionEngine(SolarPlayer player) {
        this.player = player;
        this.tracker = player.getPositionTracker();
    }

    public void resetMovement() {
        if (lastValidMovement == null) return;

        lastValidMovement.setMotion(new Vector3d());
    }

    public PredictionResult getResult() {
        PredictionResult result = new PredictionResult(player, tracker.getMotion());

        if (lastValidMovement == null) {
            lastValidMovement = new MovePossibility(player);
            return result;
        }

        lastValidMovement.setMotion(processEndOfTheTick(lastValidMovement.getMotion()));

        for (float forward = -1; forward <= 1; forward++) {
            for (float strafe = -1; strafe <= 1; strafe++) {
                PossibilityType[] possibilities = new PossibilityType[] { PossibilityType.NORMAL, PossibilityType.ITEM_SLOWDOWN };

                for (PossibilityType type : possibilities) {
                    MovePossibility possibility = new MovePossibility(player, lastValidMovement, forward * 0.98f, strafe * 0.98f, type);
                    result.addMovePossibility(possibility);

                    if (lastValidMovement.isOnGround()) {
                        MovePossibility jumpPossibility = new MovePossibility(player, lastValidMovement, forward  * 0.98f, strafe * 0.98f, PossibilityType.JUMP, type);
                        result.addMovePossibility(jumpPossibility);
                    }
                }
            }
        }

        lastValidMovement = result.getClosestPossibility();

        return result;
    }

    public Vector3d processEndOfTheTick(Vector3d motion) {
        double motionX = motion.getX(), motionY = motion.getY(), motionZ = motion.getZ();
        double threshold = player.getClientVersion().isOlderThan(ClientVersion.V_1_9) ? 0.005 : 0.003;

        motionX *= lastValidMovement.getFriction();
        motionZ *= lastValidMovement.getFriction();

        Vector3d position = player.getPositionTracker().getLastPosition();

        int chunkX = (int) position.getX() >> 4;
        int chunkZ = (int) position.getZ() >> 4;

        motionY -= 0.08;

        if (!player.getWorldTracker().isChunkLoaded(chunkX, chunkZ)) {
            motionY = -0.1;
        }

        motionY *= 0.98f;

        if (Math.abs(motionX) < threshold) motionX = 0;
        if (Math.abs(motionY) < threshold) motionY = 0;
        if (Math.abs(motionZ) < threshold) motionZ = 0;

        return new Vector3d(motionX, motionY, motionZ);
    }
}
