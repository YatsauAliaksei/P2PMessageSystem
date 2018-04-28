package by.mrj.messaging.network.transport;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.springframework.beans.factory.annotation.Value;

/**
 * Simple socket based transport which uses Socket and ServerSocket
 * as compositions to implement API Interfaces.
 */
@Log4j2
public class SimpleSocketTransport implements Transport {

    private final String netAddress;

    public SimpleSocketTransport(@Value("${app.net.address}") String netAddress) {
        this.netAddress = netAddress;
    }

    @Override
    @SneakyThrows
    public InputStream sendWithResponse(byte[] bytes, String address) {
        String[] addr = address.split(":");
        Socket socket = new Socket(addr[0], Integer.valueOf(addr[1]));
        OutputStream os = socket.getOutputStream();
        os.write(bytes);
        os.flush();
//        socket.shutdownOutput();

        return socket.getInputStream();
    }

    @Override
    public NetServerSocket listening() {
        int port = Integer.valueOf(netAddress.split(":")[1]);
        log.debug("Listening socket {}.", port);
        return new BasicNetServerSocket(port);
    }

    @Override
    public String netAddress() {
        return netAddress;
    }

    private static final class BasicNetServerSocket implements NetServerSocket {

        private ServerSocket serverSocket;

        @SneakyThrows
        private BasicNetServerSocket(int port) {
            serverSocket = new ServerSocket(port);
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
        }

        @Override
        @SneakyThrows
        public NetSocket accept() {
            Socket socket = serverSocket.accept();
            return BasicNetSocket.of(socket);
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    private static final class BasicNetSocket implements NetSocket {

        private final Socket socket;

        @Override
        @SneakyThrows
        public InputStream inputStream() {
            return socket.getInputStream();
        }

        @Override
        @SneakyThrows
        public OutputStream outputStream() {
            return socket.getOutputStream();
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }
}
