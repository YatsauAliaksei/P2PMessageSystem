package by.mrj.messaging.network.discovery;

import by.mrj.message.domain.Registration;
import lombok.SneakyThrows;

import java.util.List;

public interface DiscoveryService {

    /**
     * Returns own address in network.
     */
//    String getOwnAddress();

    /**
     * Returns full Nodes addresses.
     */
    List<String> discoverPeers();

    Registration getPeerData(String peerName);
}
