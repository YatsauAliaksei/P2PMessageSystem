package by.mrj.messaging.network;

import by.mrj.message.domain.Message;
import by.mrj.message.util.NetUtils;
import by.mrj.messaging.network.discovery.DiscoveryService;
import by.mrj.messaging.network.transport.NetServerSocket;
import by.mrj.messaging.network.transport.NetSocket;
import by.mrj.messaging.network.transport.Transport;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Log4j2
@Service
public class NetworkService {

    private final MessageProcessor operationMarshaller;
    private final DiscoveryService discoveryService;
    private final Transport transport;

    private List<String> peers;

    @Autowired
    public NetworkService(MessageProcessor operationMarshaller,
                          DiscoveryService discoveryService,
                          Transport transport) {
        this.operationMarshaller = operationMarshaller;
        this.discoveryService = discoveryService;
        this.transport = transport;
    }

    @PostConstruct
    private void init() {
        this.peers = getPeers();
        log.debug("Network has been created. [{}]", peers);

        CompletableFuture.runAsync(this::listening, Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            return thread;
        }));
    }

    private List<String> getPeers() {
        return discoveryService.discoverPeers();
    }

    @SneakyThrows
    public Message<?> sendToAddress(String address, Message<?> message) {
        log.debug("Sending message to address [{}], [{}]", message, address);
        byte[] bytes = NetUtils.serialize(message);
        try (InputStream is = transport.sendWithResponse(bytes, address)) {
            return NetUtils.deserialize(is);
        }
    }

    @SneakyThrows
    private void send(Message<?> message, NetSocket socket) {
        log.debug("Sending message to socket [{}], [{}]", message, socket);
        OutputStream os = socket.outputStream();
        byte[] bytes = NetUtils.serialize(message);
        os.write(bytes);
        os.flush();
    }

    @SneakyThrows
    public void send(Message<?> message) {
        log.debug("Sending message [{}]", message);
        Socket socket = new Socket();
        OutputStream os = socket.getOutputStream();
        os.write(NetUtils.serialize(message));
        os.flush();
    }

    public List<Message<?>> sendToNetwork(Message<?> message) {
        log.debug("Sending message to peers.");

        List<Message<?>> responses = peers.parallelStream()
                .map(discoveryService::getPeerData)
                .map(s -> sendToAddress(s.getNetworkAddress(), message)) // todo: port should be configurable
                .collect(toList());

        log.debug("Received {} responses.", responses.size());
        return responses;
    }

    private void listening() {

        try (NetServerSocket serverSocket = transport.listening()) {
            while (true) {
                log.debug("Listening socket.");

                NetSocket clientSocket = serverSocket.accept();

                CompletableFuture.runAsync(() -> {
                    try (NetSocket socket = clientSocket) {
                        Message<?> message = NetUtils.deserialize(socket.inputStream());
                        log.debug("Message received. [{}]", message);

                        Message<?> response = operationMarshaller.process(message);
                        if (response != null) {
                            send(response, socket);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e); // todo
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // todo
        }
    }
}
