package com.hc.apiproject.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * 腾讯云对象存储客户端
 */
//@Configuration
//@ConfigurationProperties(prefix = "cos.client")
//@Data
public class CosClientConfig {

    /**
     * accessKey
     */
    @Value("${spring.tencent.SecretId}")
    private String accessKey;

    /**
     * secretKey
     */
    @Value("${spring.tencent.SecretKey}")
    private String secretKey;

    /**
     * 区域
     */
    @Value("${spring.tencent.region}")
    private String region;

    /**
     * 桶名
     */
    @Value("${spring.tencent.bucket}")
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}