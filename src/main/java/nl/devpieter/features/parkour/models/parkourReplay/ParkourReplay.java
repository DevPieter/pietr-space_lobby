package nl.devpieter.features.parkour.models.parkourReplay;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.ClientPacket;
import nl.devpieter.features.parkour.models.parkour.Parkour;

import java.util.ArrayList;
import java.util.List;

public class ParkourReplay {

    private final Pos startPosition;
    private final Pos respawnPosition;

    private final List<TrackedPacket> trackedPackets = new ArrayList<>();

    private boolean tracking = false;

    public ParkourReplay(Pos startPosition, Parkour parkour) {
        this.startPosition = startPosition;
        this.respawnPosition = parkour.spawnPos();
    }

    public void startTracking() {
        trackedPackets.clear();
        tracking = true;
    }

    public void stopTracking() {
        tracking = false;
    }

    public void pushPacket(ClientPacket packet) {
        if (!tracking) return;
        trackedPackets.add(new TrackedPacket(packet, System.currentTimeMillis()));
    }

    public Pos startPosition() {
        return startPosition;
    }

    public Pos respawnPosition() {
        return respawnPosition;
    }

    public List<TrackedPacket> trackedPackets() {
        return trackedPackets;
    }
}
