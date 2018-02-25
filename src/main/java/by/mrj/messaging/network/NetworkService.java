package by.mrj.messaging.network;

import by.mrj.messaging.util.NetUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
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

    private final List<String> peers;
    private final int DEFAULT_PORT = 8338; // todo: make customizable
    private final MessageProcessor operationMarshaller;
    private final DiscoveryService discoveryService;

    @Autowired
    public NetworkService(MessageProcessor operationMarshaller, DiscoveryService discoveryService) {
        this.operationMarshaller = operationMarshaller;
        this.discoveryService = discoveryService;
        this.peers = getPeers(discoveryService);
        log.debug("Network has been created. [{}]", peers);

        CompletableFuture.runAsync(this::listening, Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            return thread;
        }));
    }

    private List<String> getPeers(DiscoveryService discoveryService) {
        return discoveryService.discoverNodes();
    }

    @SneakyThrows
    public Message<?> sendWithResponse(String address, Message<?> message) {
        try (Socket socket = new Socket(address, DEFAULT_PORT)) {
            send(message, socket);

            InputStream inputStream = socket.getInputStream();
            return NetUtils.deserialize(inputStream);
        }
    }

    @SneakyThrows
    public void send(Message<?> message, Socket socket) {
        log.debug("Sending message to socket [{}], [{}]", message, socket);
        OutputStream os = socket.getOutputStream();
        os.write(NetUtils.serialize(message));
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
                .map(s -> sendWithResponse(s, message))
                .collect(toList());

        log.debug("Received {} responses.", responses.size());
        return responses;
    }

    private void listening() {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            while (true) {
                log.debug("Listening socket {}", DEFAULT_PORT);

                Socket clientSocket = serverSocket.accept();

                CompletableFuture.runAsync(() -> {
                    try (Socket socket = clientSocket) {
                        Message<?> message = NetUtils.deserialize(socket.getInputStream());
                        log.debug("Message received. [{}]", message);

                        Message<?> response = operationMarshaller.processWithResponse(message);
                        if (response != null) {
                            send(response, socket);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e); // todo
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // todo
        }
    }
}
