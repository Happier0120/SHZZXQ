package com.example.propertysupervision.protocol.config;

import com.example.propertysupervision.protocol.inbound.InboundFrameProcessor;
import com.example.propertysupervision.protocol.inbound.InboundProtocolFrameHandler;
import com.example.propertysupervision.protocol.transport.ProtocolSocketServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;

@Configuration
@EnableConfigurationProperties(
        BankSocketServerProperties.class
)
public class BankSocketServerConfiguration {

    @Bean
    public InboundProtocolFrameHandler inboundProtocolFrameHandler(
            InboundFrameProcessor frameProcessor) {

        return new InboundProtocolFrameHandler(
                frameProcessor
        );
    }

    @Bean(
            initMethod = "start",
            destroyMethod = "close"
    )
    @ConditionalOnProperty(
            prefix = "bank.socket.server",
            name = "enabled",
            havingValue = "true"
    )
    public ProtocolSocketServer protocolSocketServer(
            BankSocketServerProperties properties,
            InboundProtocolFrameHandler frameHandler)
            throws IOException {

        if (properties.getPort() == null) {
            throw new IllegalStateException(
                    "启用银行Socket服务时必须配置监听端口"
            );
        }

        return new ProtocolSocketServer(
                InetAddress.getByName(
                        properties.getBindAddress()
                ),
                properties.getPort(),
                properties.getBacklog(),
                properties.getReadTimeoutMillis(),
                properties.getWorkerThreads(),
                frameHandler
        );
    }
}
