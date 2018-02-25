package by.mrj.messaging;

import by.mrj.messaging.network.MessageProcessor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:default.properties")
@ComponentScan("by.mrj")
@ConditionalOnBean(MessageProcessor.class)
@ConditionalOnProperty(name = {"p2p.messaging.enabled"}, havingValue = "true")
@Configuration
//@EnableAutoConfiguration
public class P2PSystemConfig {

/*    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder()
                .sources(MainConfig.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }*/
}
