package com.hc.apiproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.hcapicommon.model.entity.UserInterfaceInfo;

/**
 * @Author: Kenneth shi
 * @Description:
 **/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
