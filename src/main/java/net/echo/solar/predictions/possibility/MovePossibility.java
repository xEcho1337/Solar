package net.echo.solar.predictions.possibility;

import com.github.retrooper.packetevents.util.Vector3d;
import net.echo.solar.common.MathHelper;
import net.echo.solar.common.boundingbox.BoundingBox;
import net.echo.solar.common.boundingbox.BoundingBoxUtils;
import net.echo.solar.common.movement.ConstantsCalculator;
import net.echo.solar.player.SolarPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MovePossibility implements Cloneable {

    private final List<PossibilityType> tags;
    private final SolarPlayer player;
    private double motionX, motionY, motionZ;
    private float forward, strafe, friction, yaw;
    private boolean onGround, sprinting, shouldResetX, shouldResetZ;

    public MovePossibility(SolarPlayer player) {
        this.player = player;
        this.tags = new ArrayList<>();
    }

    public MovePossibility(SolarPlayer player, MovePossibility lastMovement, float forward, float strafe, PossibilityType... tags) {
        this.tags = new ArrayList<>(List.of(tags));

        if (this.tags.contains(PossibilityType.ITEM_SLOWDOWN)) {
            forward *= 0.2f;
            strafe *= 0.2f;
        }

        this.player = player;

        Vector3d motion = lastMovement.getMotion();

        this.motionX = motion.getX();
        this.motionY = motion.getY();
        this.motionZ = motion.getZ();

        if (lastMovement.shouldResetX) this.motionX = 0;
        if (lastMovement.shouldResetZ) this.motionZ = 0;

        this.forward = forward;
        this.strafe = strafe;
        this.onGround = lastMovement.isOnGround();
        this.sprinting = player.getAttributeTracker().isSprinting();

        this.yaw = player.getPositionTracker().getYaw();

        if (this.tags.contains(PossibilityType.JUMP)) {
            doJump();
        }

        runPrediction();
    }

    public void doJump() {
        float jumpMotion = 0.42f;

        Player bukkitPlayer = player.getBukkitPlayer();

        // TODO: Custom potion effects tracking?
        Collection<PotionEffect> potionEffects = bukkitPlayer.getActivePotionEffects();
        PotionEffect jump = potionEffects.stream().filter(x -> x.getType().equals(PotionEffectType.JUMP)).findFirst().orElse(null);

        if (jump != null) {
            jumpMotion += (float) (jump.getAmplifier() + 1) * 0.1F;
        }

        this.motionY = jumpMotion;

        if (sprinting) {
            float f = yaw * 0.017453292F;

            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }
    }

    public void runPrediction() {
        float friction = ConstantsCalculator.getFrictionFactor(player, onGround);
        float actualFriction = 0.16277136f / (friction * friction * friction);

        float airFriction = 0.02f;

        if (sprinting) {
            airFriction = (float) (airFriction + airFriction * 0.3);
        }

        float baseSpeed = (float) player.getAttributeTracker().getMovementSpeed();
        float moveSpeed = onGround ? baseSpeed * actualFriction : airFriction;

        this.friction = friction;

        moveFlying(moveSpeed);
        processCollisions();
    }

    public void moveFlying(float friction) {
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
        double initialMotionX = motionX, initialMotionY = motionY, initialMotionZ = motionZ;

        Vector3d lastPosition = player.getPositionTracker().getLastPosition();
        BoundingBox boundingBox = player.getBoundingBox(lastPosition);

        List<BoundingBox> boundingBoxes = BoundingBoxUtils.getCollidingBoundingBoxes(player,
                boundingBox.addCoord(motionX, motionY, motionZ));

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

        this.onGround = isCollidedVertically && initialMotionY < 0;

        this.shouldResetX = initialMotionX != motionX;
        this.shouldResetZ = initialMotionZ != motionZ;
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
