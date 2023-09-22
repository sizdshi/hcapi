package com.hc.apiproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.hcapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
 * @author sizd-shi
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
 * @createDate 2023-08-12 15:28:20
 * @Entity com.hc.hcapicommon.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




