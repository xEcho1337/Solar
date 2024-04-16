package net.echo.solar.listeners.trackers.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackedEntity {

    private final int entityId;
    private final Map<String, Property> properties = new ConcurrentHashMap<>();
    private Vector3d position = new Vector3d();

    public TrackedEntity(int entityId) {
        this.entityId = entityId;
    }

    public void move(double motionX, double motionY, double motionZ) {
        this.position = position.add(motionX, motionY, motionZ);
    }

    public void setProperty(Property property) {
        this.properties.put(property.getKey(), property);
    }

    public int getEntityId() {
        return entityId;
    }

    public Collection<Property> getProperties() {
        return properties.values();
    }

    public Property getProperty(String key) {
        return this.properties.get(key);
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }
}
