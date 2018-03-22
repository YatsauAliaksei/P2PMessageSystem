package by.mrj.messaging.network.transport;

public interface NetServerSocket extends AutoCloseable {
    NetSocket accept();
}
