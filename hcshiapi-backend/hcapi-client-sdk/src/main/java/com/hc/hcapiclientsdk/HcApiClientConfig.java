package com.hc.hcapiclientsdk;

import com.hc.hcapiclientsdk.client.HcApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @创建人 Alexshi
 * @创建时间 2023/8/3
 * @描述
 */

@Configuration
@ConfigurationProperties("hcapi.client")
@Data
@ComponentScan
public class HcApiClientConfig {
    private String accessKey;

    private String secretKey;

    @Bean
    public HcApiClient hcApiClient(){
        return new HcApiClient(accessKey,secretKey);
    }

}
