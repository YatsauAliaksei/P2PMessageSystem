package by.mrj.messaging.network;

import by.mrj.crypto.util.CryptoUtils;
import by.mrj.crypto.util.EncodingUtils;
import by.mrj.messaging.network.domain.Registration;
import by.mrj.messaging.util.NetUtils;
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
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Quotas;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

@Getter
@Log4j2
@Service
public class DiscoveryService {

    private final CuratorFramework zkClient;
    private final String appName;
    private final String peersPath;
    private final String ip;

    @Autowired
    public DiscoveryService(@Value("${discovery.service.connection}") String connection,
                            @Value("${app.root.node.name}") String appName) {
        this.appName = appName;
        this.ip = getWorldIp();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(connection)
                .retryPolicy(retryPolicy)
//                .authorization("digest", "someThing signed with private key or public key it self.".getBytes())
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
        log.debug("Curator client has started.");

        peersPath = "/app/" + this.appName + "/peers";

        createRootPeersPath();
        registerAsAPeer();
    }

    @SneakyThrows
    String getWorldIp() { // better to have multiple calls to diff services to determine world ip.
        return Request.Get("https://ifconfig.co/ip")
                .execute().handleResponse(httpResponse -> {
                    HttpEntity entity = httpResponse.getEntity();
                    return entity != null ? new Content(EntityUtils.toByteArray(entity), ContentType.getOrDefault(entity)) : Content.NO_CONTENT;
                }).asString().trim();
    }

    public List<String> discoverNodes() {
        return silent(zkClient.getChildren(), peersPath);
    }

    @SneakyThrows
    public <T> T getNodeData(String nodeName, Class<T> clazz) {
        byte[] bytes = zkClient.getData().forPath(peersPath + "/" + nodeName);
        return NetUtils.deserialize(bytes, clazz);
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

    private void registerAsAPeer() {
        String address = CryptoUtils.sha256ripemd160(EncodingUtils.HEX.encode(CryptoUtils.pubKey));
        Registration registration = Registration.builder()
                .address(address)
                .ip(ip)
                .build();

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
