package net.echo.solar.player.data;

public class PacketData {

    // 1.17 duplicates
    private boolean isDuplicate;
    private boolean isTeleport;

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.isDuplicate = duplicate;
    }

    public boolean isTeleport() {
        return isTeleport;
    }

    public void setTeleport(boolean teleport) {
        this.isTeleport = teleport;
    }
}
