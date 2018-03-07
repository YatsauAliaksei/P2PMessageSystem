package by.mrj.messaging.network;

public interface MessageProcessor {

    /**
     * Process message with response.
     * @param message - request Message
     * @return - response message
     */
    Message<?> process(Message<?> message);
}
