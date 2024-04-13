package net.echo.solar.common.boundingbox;

import com.github.retrooper.packetevents.util.Vector3d;

public class BoundingBox {

    private Vector3d minimum;
    private Vector3d maximum;

    public BoundingBox(Vector3d minimum, Vector3d maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Vector3d center() {
        return new Vector3d((minX() + maxX()) / 2, (minY() + maxY()) / 2, (minZ() + maxZ()) / 2);
    }

    public static BoundingBox fromBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Vector3d minVector = new Vector3d(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ));
        Vector3d maxVector = new Vector3d(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));

        return new BoundingBox(minVector, maxVector);
    }

    public static BoundingBox fromPositionAndSize(Vector3d position, double width, double height) {
        Vector3d minVector = new Vector3d(position.getX() - width / 2, position.getY(), position.getZ() - width / 2);
        Vector3d maxVector = new Vector3d(position.getX() + width / 2, position.getY() + height, position.getZ() + width / 2);

        return new BoundingBox(minVector, maxVector);
    }

    public double minX() {
        return minimum.getX();
    }

    public double minY() {
        return minimum.getY();
    }

    public double minZ() {
        return minimum.getZ();
    }

    public double maxX() {
        return maximum.getX();
    }

    public double maxY() {
        return maximum.getY();
    }

    public double maxZ() {
        return maximum.getZ();
    }

    public void expand(double moveThreshold) {
        minimum = minimum.subtract(moveThreshold, moveThreshold, moveThreshold);
        maximum = maximum.add(moveThreshold, moveThreshold, moveThreshold);
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "minimum=" + minimum +
                ", maximum=" + maximum +
                '}';
    }

    public boolean collidesVertically(BoundingBox box) {
        return box.maxX() > this.minX()
                && box.minX() < this.maxX()
                && box.maxY() >= this.minY()
                && box.minY() <= this.maxY()
                && box.maxZ() > this.minZ()
                && box.minZ() < this.maxZ();
    }

    public boolean collides(BoundingBox other) {
        return (this.maxX() > other.minX()
                && this.minX() < other.maxX()
                && this.maxY() > other.minY()
                && this.minY() < other.maxY()
                && this.maxZ() > other.minZ()
                && this.minZ() < other.maxZ());
    }

    public boolean intersectsWith(BoundingBox other) {
        return other.maxX() > this.minX()
                && other.minX() < this.maxX()
                && (other.maxY() > this.minY()
                && other.minY() < this.maxY()
                && other.maxZ() > this.minZ()
                && other.minZ() < this.maxZ());
    }

    public BoundingBox offset(int x, int y, int z) {
        return new BoundingBox(this.minimum.add(x, y, z), this.maximum.add(x, y, z));
    }

    public BoundingBox offset(double x, double y, double z) {
        return new BoundingBox(this.minimum.add(x, y, z), this.maximum.add(x, y, z));
    }

    public BoundingBox offsetNew(double x, double y, double z) {
        return new BoundingBox(this.minimum.add(x, y, z), this.maximum.add(x, y, z));
    }

    public BoundingBox addCoord(double x, double y, double z) {
        double minX = this.minX();
        double minY = this.minY();
        double minZ = this.minZ();
        double maxX = this.maxX();
        double maxY = this.maxY();
        double maxZ = this.maxZ();

        if (x < 0.0D) {
            minX += x;
        } else if (x > 0.0D) {
            maxX += x;
        }

        if (y < 0.0D) {
            minY += y;
        } else if (y > 0.0D) {
            maxY += y;
        }

        if (z < 0.0D) {
            minZ += z;
        } else if (z > 0.0D) {
            maxZ += z;
        }

        return BoundingBox.fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public double calculateXOffset(BoundingBox other, double offsetX) {
        if (other.maxY() > this.minY() && other.minY() < this.maxY() && other.maxZ() > this.minZ() && other.minZ() < this.maxZ()) {
            if (offsetX > 0.0D && other.maxX() <= this.minX()) {
                final double d1 = this.minX() - other.maxX();

                if (d1 < offsetX) {
                    offsetX = d1;
                }
            } else if (offsetX < 0.0D && other.minX() >= this.maxX()) {
                final double d0 = this.maxX() - other.minX();

                if (d0 > offsetX) {
                    offsetX = d0;
                }
            }
        }

        return offsetX;
    }

    public double calculateYOffset(BoundingBox other, double offsetY) {
        if (other.maxX() > this.minX() && other.minX() < this.maxX() && other.maxZ() > this.minZ() && other.minZ() < this.maxZ()) {
            if (offsetY > 0.0D && other.maxY() <= this.minY()) {
                final double d1 = this.minY() - other.maxY();

                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.minY() >= this.maxY()) {
                final double d0 = this.maxY() - other.minY();

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }
        }

        return offsetY;
    }

    public double calculateZOffset(BoundingBox other, double offsetZ) {
        if (other.maxX() > this.minX() && other.minX() < this.maxX() && other.maxY() > this.minY() && other.minY() < this.maxY()) {
            if (offsetZ > 0.0D && other.maxZ() <= this.minZ()) {
                final double d1 = this.minZ() - other.maxZ();

                if (d1 < offsetZ) {
                    offsetZ = d1;
                }
            } else if (offsetZ < 0.0D && other.minZ() >= this.maxZ()) {
                final double d0 = this.maxZ() - other.minZ();

                if (d0 > offsetZ) {
                    offsetZ = d0;
                }
            }
        }

        return offsetZ;
    }
}
