package net.echo.solar.player.data;

public class PacketData {

    private boolean exemptIs117Duplicate;
    private boolean exemptIsTeleport;

    public boolean isExemptIs117Duplicate() {
        return exemptIs117Duplicate;
    }

    public void setExemptIs117Duplicate(boolean exemptIs117Duplicate) {
        this.exemptIs117Duplicate = exemptIs117Duplicate;
    }

    public boolean isExemptIsTeleport() {
        return exemptIsTeleport;
    }

    public void setExemptIsTeleport(boolean exemptIsTeleport) {
        this.exemptIsTeleport = exemptIsTeleport;
    }
}
