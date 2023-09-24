package com.hc.apiproject.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hc.gateway")
@Data
public class GatewayConfig {

    private String host;

}
