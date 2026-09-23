package nl.devpieter.features.parkour.models.replay;

import net.minestom.server.network.packet.client.ClientPacket;

record TrackedPacket(ClientPacket packet, long timestamp) {
}
