package com.hcshi.apiproject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcshi.apiproject.model.entity.UserInterfaceInfo;
import com.hcshi.apiproject.service.UserInterfaceInfoService;
import com.hcshi.apiproject.mapper.UserInterfaceInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author sizd-shi
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-08-12 15:28:20
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

}




