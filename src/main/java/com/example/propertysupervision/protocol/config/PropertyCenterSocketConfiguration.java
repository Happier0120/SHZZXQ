package com.example.propertysupervision.protocol.config;

import com.example.propertysupervision.protocol.transport.SocketProtocolClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PropertyCenterSocketProperties.class)
public class PropertyCenterSocketConfiguration {

    @Bean
    public SocketProtocolClient socketProtocolClient(PropertyCenterSocketProperties properties) {

        return new SocketProtocolClient(
            properties.getHost(),
            properties.getPort(),
            properties.getConnectTimeoutMillis(),
            properties.getReadTimeoutMillis()
        );
    }
}