package by.mrj.messaging.network;

public interface MessageProcessor {

    /**
     * Process message with response.
     * @param message - request Message
     * @return - response message
     */
    Message<?> processWithResponse(Message<?> message);

    /**
     * Process message.
     * @param message - request Message
     */
    void process(Message<?> message);
}
