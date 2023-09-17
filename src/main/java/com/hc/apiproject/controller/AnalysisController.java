package com.hc.apiproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.apiproject.annotation.AuthCheck;
import com.hc.apiproject.common.BaseResponse;
import com.hc.apiproject.common.ErrorCode;
import com.hc.apiproject.common.ResultUtils;
import com.hc.apiproject.exception.BusinessException;
import com.hc.apiproject.mapper.UserInterfaceInfoMapper;
import com.hc.apiproject.service.InterfaceInfoService;
import com.hc.hcapicommon.model.entity.InterfaceInfo;
import com.hc.hcapicommon.model.entity.UserInterfaceInfo;
import com.hc.hcapicommon.model.vo.InterfaceInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Kenneth shi
 * @Description:
 **/
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {


    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping("/top/inteface/invoke")
    @AuthCheck
    public BaseResponse<List<InterfaceInfoVO>> listBaseResponse() {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);
    }
}

