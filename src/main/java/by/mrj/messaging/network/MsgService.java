package by.mrj.messaging.network;

import by.mrj.message.domain.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
// todo: works like proxy. Maybe should be removed.
public class MsgService {

    private NetworkService networkService;

    @Autowired
    public MsgService(NetworkService networkService) {
        this.networkService = networkService;
    }

    // TODO
    public void sendMessageToNetwork(Message<?> message) {
        log.debug("Sending message to network [{}]", message);
        List<Message<?>> response = networkService.sendToNetwork(message);
        log.debug("Network response: {}", response);
    }

    public void sendMessage(Message<?> message, String address) {
        log.debug("Sending message [{}] to address [{}].", message);
        Message<?> response = networkService.sendToAddress(address, message);
        log.debug("Response: {}", response);
    }
}
