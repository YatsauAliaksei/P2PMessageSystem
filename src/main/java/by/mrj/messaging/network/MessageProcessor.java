package by.mrj.messaging.network;

import by.mrj.message.domain.Message;

public interface MessageProcessor {

    /**
     * Process message with response.
     * @param message - request Message
     * @return - response message
     */
    Message<?> process(Message<?> message);
}
