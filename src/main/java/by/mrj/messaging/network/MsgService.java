package by.mrj.messaging.network;

import by.mrj.message.domain.Message;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MsgService {

    private NetworkService networkService;

    @Autowired
    public MsgService(NetworkService networkService) {
        this.networkService = networkService;
    }

    // TODO
    public void sendMessage(Message<?> message) {
        log.debug("Sending message [{}]", message);
        List<Message<?>> response = networkService.sendToNetwork(message);
        log.debug("Response: {}", response);
    }

}
