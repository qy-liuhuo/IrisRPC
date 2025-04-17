package io.github.qylh.iris.spring.boot;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "iris")
public class IrisProperties {

    private String broker;

    private String username;

    private String password;

    private String clientId;

    private int connectionTimeout;

    private int keepAliveInterval;

    private int timeout = 10;

}
