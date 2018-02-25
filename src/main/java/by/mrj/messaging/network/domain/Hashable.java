package by.mrj.messaging.network.domain;

public interface Hashable {
    /**
     * Returns JSON object representation of it's immutable part.
     */
    String hash();
}
