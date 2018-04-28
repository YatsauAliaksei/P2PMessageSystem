package by.mrj.messaging;

import by.mrj.messaging.network.MessageProcessor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("by.mrj.messaging")
@ConditionalOnBean(MessageProcessor.class)
@Configuration
public class P2PSystemConfig {

}
