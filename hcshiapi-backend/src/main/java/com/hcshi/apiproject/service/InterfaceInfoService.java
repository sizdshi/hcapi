package com.hcshi.apiproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hcshi.apiproject.model.entity.InterfaceInfo;


/**
* @author sizd-shi
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-07-10 16:47:40
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
