package by.mrj.messaging;

import by.mrj.messaging.network.MessageProcessor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("by.mrj.messaging")
@ConditionalOnBean(MessageProcessor.class)
@ConditionalOnProperty(name = {"p2p.messaging.enabled"}, havingValue = "true")
@Configuration
public class P2PSystemConfig {

}
