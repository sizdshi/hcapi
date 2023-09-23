package com.hc.apiproject.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;

import static com.hc.apiproject.constant.FileConstant.COS_HOST;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

//    @Resource
//    private CosClientConfig cosClientConfig;
//
//    @Resource
//    private COSClient cosClient;

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


    private COSClient cosClient;

    private void cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        cosClient = new COSClient(cred, clientConfig);
    }


    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
//    public PutObjectResult putObject(String key, String localFilePath) {
//        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
//                new File(localFilePath));
//
//        return cosClient.putObject(putObjectRequest);
//    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
//    public PutObjectResult putObject(String key, File file) {
//
//        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
//                file);
//
//        return cosClient.putObject(putObjectRequest);
////        try {
////            String name = file.getName();
////            String key = directory + UUID.randomUUID() + name.substring(name.lastIndexOf("."));
////            PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
////            cosClient.putObject(putObjectRequest);
////            return "这里填腾讯云对象存储访问的域名" + key;
////        } catch (CosClientException clientException) {
////            clientException.printStackTrace();
////            return "";
////        }
//    }

    /**
     * 上传文件
     */
    public String upLoad(String directory, File file) {
        cosClient();
        try {
            String name = file.getName();
            String key = directory + UUID.randomUUID() + name.substring(name.lastIndexOf("."));
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
            cosClient.putObject(putObjectRequest);
            return COS_HOST + "/" + key;
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
            return "";
        }
    }

}
