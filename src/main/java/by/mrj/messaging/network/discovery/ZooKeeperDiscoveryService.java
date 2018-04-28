package by.mrj.messaging.network.discovery;

import by.mrj.crypto.util.CryptoUtils;
import by.mrj.crypto.util.EncodingUtils;
import by.mrj.message.domain.Registration;
import by.mrj.message.types.Command;
import by.mrj.message.util.NetUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Quotas;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Value;
import com.google.common.collect.Lists;

@Getter
@Log4j2
public class ZooKeeperDiscoveryService implements DiscoveryService {

    private final CuratorFramework zkClient;
    private final String appName;
    private final String peersPath;
//    private final String ip;

    public ZooKeeperDiscoveryService(@Value("${discovery.service.connection}") String connection,
                                     @Value("${app.root.node.name}") String appName,
                                     @Value("${peer.net.address}") String netAddress) {
        this.appName = appName;
//        this.ip = getOwnAddress();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(connection)
                .retryPolicy(retryPolicy)
                .authorization("digest", "someThing signed with private key.".getBytes())
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                }).build();

        zkClient.start();
        log.debug("Curator client has been started.");

        peersPath = "/app/" + this.appName + "/peers";

        createRootPeersPath();
        registerAsAPeer(netAddress);
    }

/*    @SneakyThrows
    @Override
    public String getOwnAddress() { // better to have multiple calls to diff services to determine world ip.
        return Request.Get("https://ifconfig.co/ip") // fix: broken. Will return NAT address.
                .execute().handleResponse(httpResponse -> {
                    HttpEntity entity = httpResponse.getEntity();
                    return entity != null ? new Content(EntityUtils.toByteArray(entity), ContentType.getOrDefault(entity)) : Content.NO_CONTENT;
                }).asString().trim();
    }*/

    @Override
    public List<String> discoverPeers() {
        // todo: remove own path.
        return silent(zkClient.getChildren(), peersPath);
//        return nodes.stream()
//                .map(node -> getPeerData(node, Registration.class))
//                .collect(toList());
    }

    @SneakyThrows
    @Override
    public Registration getPeerData(String peerName) {
        log.debug("Getting node [{}] of type [{}]", peerName, Command.REGISTRATION);
        byte[] bytes = zkClient.getData().forPath(peersPath + "/" + peerName);
        return NetUtils.deserialize(bytes, Registration.class);
    }

    public List<String> returnPathRecursively(String path) {
        Queue<String> queue = new ArrayDeque<>();
        List<String> paths = new ArrayList<>();
        queue.add(path);
        while (!queue.isEmpty()) {
            String s = queue.poll();
            List<String> children = silent(zkClient.getChildren(), s);
            if (!children.isEmpty()) {
                for (String child : children) {
                    String e = s + ("/".equals(s) ? "" : "/") + child;
                    paths.add(e);
                    queue.add(e);
                }
            }
        }

        // removing from list ZooKeeper's paths
        paths.removeAll(Lists.asList(Quotas.procZookeeper, Quotas.quotaZookeeper, new String[0]));
        return paths;
    }

    private void registerAsAPeer(String netAddress) {
        String address = CryptoUtils.sha256ripemd160(EncodingUtils.HEX.encode(CryptoUtils.pubKey));
        Registration registration = Registration.builder()
                .address(address)
                .networkAddress(netAddress)
                .build();

        if (log.isDebugEnabled()) {
            List<String> list = returnPathRecursively("/");
            log.debug("All paths: [{}]", list);
        }

        silent(zkClient.create()
                .withMode(CreateMode.EPHEMERAL), peersPath + "/" + address, registration);
    }

    private void createRootPeersPath() {
        silent(zkClient.create()
                .orSetData()
                .creatingParentsIfNeeded()
//                .withACL(Lists.newArrayList(ZooDefs.Ids.CREATOR_ALL_ACL))
                .withMode(CreateMode.PERSISTENT), peersPath);
    }

    @SneakyThrows
    private <T> T silent(PathAndBytesable<T> pathAndBytesable, String path) {
        return pathAndBytesable.forPath(path);
    }

    @SneakyThrows
    private <T> T silent(PathAndBytesable<T> pathAndBytesable, String path, Serializable data) {
        return pathAndBytesable.forPath(path, NetUtils.serialize(data, false));
    }

    @SneakyThrows
    private List<String> silent(GetChildrenBuilder getChildrenBuilder, String path) {
        return getChildrenBuilder.forPath(path);
    }

    @Override
    protected void finalize() throws Throwable {
        zkClient.close();
        super.finalize();
    }
}
