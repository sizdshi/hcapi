package com.hc.hcapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @创建人 Alexshi
 * @创建时间 2023/8/3
 * @描述
 */
public class SignUtils {
    public static String getSign(String body,String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        String  content = body+"."+secretKey;
        return md5.digestHex(content);
    }
}
