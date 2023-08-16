package com.hc.hcapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import com.hc.hcapiclientsdk.model.User;


import java.util.HashMap;
import java.util.Map;

import static com.hc.hcapiclientsdk.utils.SignUtils.getSign;


/**
 * @创建人 Alexshi
 * @创建时间 2023/8/1
 * @描述  调用第三方接口
 */
public class HcApiClient {

    private String accessKey;
    private String secretKey;


    public HcApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }


    public String  getNameByGet(String name){
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result= HttpUtil.get("http://localhost:8100/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByPost( String name){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result= HttpUtil.post("http://localhost:8100/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getUserNameByPost(User user){

        String json = JSONUtil.toJsonStr(user);
         HttpResponse httpResponse = HttpRequest.post("http://localhost:8100/api/name/user")
                 .addHeaders(getHeaderMap(json))
                 .body(json)
                .execute();

        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        return result;
    }

    private Map<String,String> getHeaderMap(String body){

        Map<String,String> hashMap = new HashMap<>();
        hashMap.put("accessKey",accessKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body",body);
        hashMap.put("timestamp",String.valueOf(System.currentTimeMillis()/1000));
        hashMap.put("sign",getSign(body,secretKey));

        return hashMap;
    }


}
