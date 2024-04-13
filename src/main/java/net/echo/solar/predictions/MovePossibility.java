package net.echo.solar.predictions;

import com.github.retrooper.packetevents.util.Vector3d;
import net.echo.solar.common.MathHelper;
import net.echo.solar.common.boundingbox.BoundingBox;
import net.echo.solar.common.boundingbox.BoundingBoxUtils;
import net.echo.solar.common.movement.ConstantsCalculator;
import net.echo.solar.player.SolarPlayer;

import java.util.List;

public class MovePossibility implements Cloneable {

    private final SolarPlayer player;
    private double motionX;
    private double motionY;
    private double motionZ;
    private float forward;
    private float strafe;
    private float friction;
    private boolean onGround;

    public MovePossibility(SolarPlayer player) {
        this.player = player;
    }

    public MovePossibility(SolarPlayer player, float forward, float strafe, MovePossibility lastMovement) {
        this.player = player;

        Vector3d motion = lastMovement.getMotion();

        this.motionX = motion.getX();
        this.motionY = motion.getY();
        this.motionZ = motion.getZ();

        this.forward = forward;
        this.strafe = strafe;
        this.onGround = lastMovement.isOnGround();

        runPrediction(player.getPositionTracker().getYaw());
    }

    public void runPrediction(float yaw) {
        float friction = ConstantsCalculator.getFrictionFactor(player, onGround);

        float actualFriction = 0.16277136f / (friction * friction * friction);
        float moveSpeed = onGround ? 0.1f * actualFriction : 0.02f;

        this.friction = friction;

        moveFlying(yaw, moveSpeed);
        processCollisions();
    }

    public void moveFlying(float yaw, float friction) {
        float magnitude = strafe * strafe + forward * forward;

        if (magnitude >= 1.0E-4F) {
            magnitude = (float) Math.sqrt(magnitude);

            if (magnitude < 1.0F) {
                magnitude = 1.0F;
            }

            magnitude = friction / magnitude;

            strafe *= magnitude;
            forward *= magnitude;

            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);

            motionX += strafe * f2 - forward * f1;
            motionZ += forward * f2 + strafe * f1;
        }
    }

    // TODO: Fix collisions
    public void processCollisions() {
        Vector3d lastPosition = player.getPositionTracker().getLastPosition();
        BoundingBox boundingBox = player.getBoundingBox(lastPosition);

        List<BoundingBox> boundingBoxes = BoundingBoxUtils.getCollidingBoundingBoxes(player, boundingBox.addCoord(motionX, motionY, motionZ));
        double initialMotionX = motionX, initialMotionY = motionY, initialMotionZ = motionZ;

        for (BoundingBox collisionBox : boundingBoxes) {
            motionY = collisionBox.calculateYOffset(boundingBox, motionY);
        }

        boundingBox = boundingBox.offset(0, motionY, 0);

        for (BoundingBox collisionBox : boundingBoxes) {
            motionX = collisionBox.calculateXOffset(boundingBox, motionX);
        }

        boundingBox = boundingBox.offset(motionX, 0, 0);

        for (BoundingBox collisionBox : boundingBoxes) {
            motionZ = collisionBox.calculateZOffset(boundingBox, motionZ);
        }

        boolean isCollidedVertically = initialMotionY != motionY;

        this.onGround = player.getPositionTracker().isOnGround();
        this.motionY = onGround ? 0 : motionY;
        // this.onGround = isCollidedVertically && initialMotionY < 0;
    }

    public Vector3d getMotion() {
        return new Vector3d(motionX, motionY, motionZ);
    }

    public void setMotion(Vector3d motion) {
        this.motionX = motion.getX();
        this.motionY = motion.getY();
        this.motionZ = motion.getZ();
    }

    public double getMotionX() {
        return motionX;
    }

    public void setMotionX(double motionX) {
        this.motionX = motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public void setMotionY(double motionY) {
        this.motionY = motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public void setMotionZ(double motionZ) {
        this.motionZ = motionZ;
    }

    public float getForward() {
        return forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getStrafe() {
        return strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public MovePossibility clone() {
        try {
            return (MovePossibility) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(System.out);
        }

        return null;
    }
}
