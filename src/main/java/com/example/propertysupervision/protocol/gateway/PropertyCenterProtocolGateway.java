package com.example.propertysupervision.protocol.gateway;

import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.transport.SocketProtocolClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PropertyCenterProtocolGateway {

    private final SocketProtocolClient socketProtocolClient;

    public PropertyCenterProtocolGateway(SocketProtocolClient socketProtocolClient) {
        this.socketProtocolClient = socketProtocolClient;
    }

    public DecodedMessage exchange(ProtocolMessage request) {
        try {
            return socketProtocolClient.exchange(request);
        } catch (IOException exception) {
            throw new PropertyCenterCommunicationException(
                "与物业中心Socket通信失败",
                exception
            );
        }
    }
}