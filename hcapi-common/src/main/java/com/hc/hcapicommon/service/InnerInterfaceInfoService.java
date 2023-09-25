package com.hc.hcapicommon.service;


import com.hc.hcapicommon.model.entity.InterfaceInfo;


/**
 * @author sizd-shi
 * @description 内部接口信息服务
 * @createDate 2023-08-12 15:28:20
 */
public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
