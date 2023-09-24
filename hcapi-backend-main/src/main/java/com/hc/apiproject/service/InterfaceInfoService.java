package com.hc.apiproject.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.hcapiclientsdk.client.HcApiClient;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.hc.hcapicommon.model.entity.InterfaceInfo;
import com.hc.hcapicommon.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;


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

    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 分页获取接口信息封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);


    /**
     * 根据用户ID 分页获取接口信息封装
     *
     * @param interfaceInfoPage 接口信息分页
     * @param request           当前会话
     * @return 接口信息分页
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOByUserIdPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 创建SDK客户端
     *
     * @param request 当前会话
     * @return SDK客户端
     */
    HcApiClient getHcApiClient(HttpServletRequest request);

    /**
     * 修改接口信息
     *
     * @param interfaceInfoUpdateRequest 接口信息修改请求
     * @return 是否成功
     */
    boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest);

    /**
     * 获取接口信息封装
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);
}
