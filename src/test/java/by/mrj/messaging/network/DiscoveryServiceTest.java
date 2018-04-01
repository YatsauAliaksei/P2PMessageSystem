package by.mrj.messaging.network;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class DiscoveryServiceTest {

    private static DiscoveryService discoveryService;

    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void getWorldIp() {
        String ip = discoveryService().getWorldIp();

        assertThat(ip).isNotEmpty();
        assertThat(ip.length() > 6).isTrue();

        log.info("My ip [{}]", ip);
    }

    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void discoverNodes() {
        List<String> peers = discoveryService().discoverNodes();
        log.info("Peers found [{}]", peers);

        assertThat(peers.size() > 0).isTrue();
    }

    @Test
//    @Ignore("Integration test. Zookeeper instance needed.")
    public void returnPathRecursively() {
        List<String> paths = discoveryService().returnPathRecursively("/");
        assertThat(paths.size() > 3).isTrue();
    }

    private static DiscoveryService discoveryService() {
        if (discoveryService == null) {
            discoveryService = new DiscoveryService("192.168.2.111:2181", "main", "my.net.address");
        }
        return discoveryService;
    }
}