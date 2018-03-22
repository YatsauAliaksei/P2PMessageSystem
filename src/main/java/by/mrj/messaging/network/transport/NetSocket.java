package by.mrj.messaging.network.transport;

import java.io.InputStream;
import java.io.OutputStream;

public interface NetSocket extends AutoCloseable {

    InputStream inputStream();

    OutputStream outputStream();
}
