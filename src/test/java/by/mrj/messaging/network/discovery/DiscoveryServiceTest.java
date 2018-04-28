package by.mrj.messaging.network.discovery;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class DiscoveryServiceTest {

    private static ZooKeeperDiscoveryService discoveryService;

/*    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void getWorldIp() {
        String ip = discoveryService().getOwnAddress();

        assertThat(ip).isNotEmpty();
        assertThat(ip.length() > 6).isTrue();

        log.info("My ip [{}]", ip);
    }*/

    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void discoverNodes() {
        val peers = discoveryService().discoverPeers();
        log.info("Peers found [{}]", peers);

        assertThat(peers.size() > 0).isTrue();
    }

    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void returnPathRecursively() {
        List<String> paths = discoveryService().returnPathRecursively("/");
        assertThat(paths.size() > 3).isTrue();
    }

    private static ZooKeeperDiscoveryService discoveryService() {
        if (discoveryService == null) {
            discoveryService = new ZooKeeperDiscoveryService("192.168.2.111:2181", "main", "my.net.address");
        }
        return discoveryService;
    }
}