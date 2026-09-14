package com.example.propertysupervision.protocol.config;

import com.example.propertysupervision.protocol.transport.SocketProtocolClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BankSocketProperties.class)
public class SocketProtocolConfiguration {

    @Bean
    public SocketProtocolClient socketProtocolClient(BankSocketProperties properties) {

        return new SocketProtocolClient(
            properties.getHost(),
            properties.getPort(),
            properties.getConnectTimeoutMillis(),
            properties.getReadTimeoutMillis()
        );
    }
}