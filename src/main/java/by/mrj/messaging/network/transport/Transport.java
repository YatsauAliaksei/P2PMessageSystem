package by.mrj.messaging.network.transport;

import java.io.InputStream;

public interface Transport {
    InputStream sendWithResponse(byte[] bytes, String address);

    NetServerSocket listening();

    String netAddress();
}
