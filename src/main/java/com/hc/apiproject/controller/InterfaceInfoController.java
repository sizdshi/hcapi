package com.hc.apiproject.controller;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.hc.apiproject.annotation.AuthCheck;
import com.hc.apiproject.config.GatewayConfig;
import com.hc.apiproject.constant.CommonConstant;
import com.hc.apiproject.constant.UserConstant;
import com.hc.apiproject.exception.BusinessException;
import com.hc.apiproject.exception.ThrowUtils;
import com.hc.apiproject.service.InterfaceInfoService;
import com.hc.apiproject.service.UserService;
import com.hc.hcapiclientsdk.client.HcApiClient;
import com.hc.hcapicommon.common.*;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.hc.hcapicommon.model.entity.InterfaceInfo;
import com.hc.hcapicommon.model.entity.User;
import com.hc.hcapicommon.model.enums.InterfaceInfoStatusEnum;
import com.hc.hcapicommon.model.vo.InterfaceInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private GatewayConfig gatewayConfig;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);

        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());

        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * 管理员上线接口
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断此接口是否存在
        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        //判断接口是否可以调用
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // 接口请求地址
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();

        HcApiClient hcApiClient = interfaceInfoService.getHcApiClient(request);
        // 设置网关地址
        hcApiClient.setGatewayHost(gatewayConfig.getHost());
        try {
            // 执行方法
            String invokeResult = hcApiClient.invokeInterface(requestParams, url, method);
            if (StringUtils.isBlank(invokeResult)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }


        //仅本人或者管理员可修改
        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
        newInterfaceInfo.setId(id);
        newInterfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());

        boolean result = interfaceInfoService.updateById(newInterfaceInfo);
        return ResultUtils.success(result);
    }


    /**
     * 下线
     *
     * @param idRequest
     * @param
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        Long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 模拟接口测试调用
     *
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        if (oldInterfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.OFFLINE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }
        // 接口请求地址
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // 获取SDK客户端
        HcApiClient neroApiClient = interfaceInfoService.getHcApiClient(request);
        // 设置网关地址
        neroApiClient.setGatewayHost(gatewayConfig.getHost());
        String invokeResult = null;
        try {
            // 执行方法
            invokeResult = neroApiClient.invokeInterface(requestParams, url, method);
            if (StringUtils.isBlank(invokeResult)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }
        return ResultUtils.success(invokeResult);

    }


    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param
     * @return
     */
    //@AutoCheck(mustRole = "admin")
    @PostMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {

        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        // 限制爬虫
        //ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);

        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询条件
     * @param request                   请求
     * @return 分页列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();

        interfaceInfoQueryRequest.setSortField("createTime");
        // 倒序排序
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size)
                , interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));

        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoByUserIdPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                             HttpServletRequest request) {

        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

//        User loginUser = userService.getLoginUser(request);
//        interfaceInfoQueryRequest.setUserId(loginUser.getId());

        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();

        interfaceInfoQueryRequest.setSortField("createTime");
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));

        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOByUserIdPage(interfaceInfoPage, request));
    }

    // endregion


    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        String description = interfaceInfoQuery.getDescription();

        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);

        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(org.apache.commons.lang3.StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(org.apache.commons.lang3.StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 编辑（用户）
     *
     * @param interfaceInfoEditRequest
     * @param requ
     * @return
     */
//    @PostMapping("/edit")
//    public BaseResponse<Boolean> editInterfaceInfo(@RequestBody InterfaceInfoEditRequest interfaceInfoEditRequest, HttpServletRequest request) {
//        if (interfaceInfoEditRequest == null || interfaceInfoEditRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        InterfaceInfo interfaceInfo = new InterfaceInfo();
//        BeanUtils.copyProperties(interfaceInfoEditRequest, interfaceInfo);
//        List<String> tags = interfaceInfoEditRequest.getTags();
//        if (tags != null) {
//            interfaceInfo.setTags(GSON.toJson(tags));
//        }
//        // 参数校验
//        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
//        User loginUser = userService.getLoginUser(request);
//        long id = interfaceInfoEditRequest.getId();
//        // 判断是否存在
//        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
//        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可编辑
//        if (!oldInterfaceInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = interfaceInfoService.updateById(interfaceInfo);
//        return ResultUtils.success(result);
//    }

}
