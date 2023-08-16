package com.hcshi.apiproject.controller;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.hc.hcapiclientsdk.client.HcApiClient;
import com.hcshi.apiproject.annotation.AuthCheck;
import com.hcshi.apiproject.common.*;
import com.hcshi.apiproject.constant.CommonConstant;
import com.hcshi.apiproject.constant.UserConstant;
import com.hcshi.apiproject.exception.BusinessException;
import com.hcshi.apiproject.exception.ThrowUtils;

import com.hcshi.apiproject.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.hcshi.apiproject.model.entity.InterfaceInfo;

import com.hcshi.apiproject.model.entity.User;
import com.hcshi.apiproject.model.enums.InterfaceInfoStatusEnum;
import com.hcshi.apiproject.service.InterfaceInfoService;
import com.hcshi.apiproject.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private HcApiClient hcApiClient;

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
//        List<String> tags = interfaceInfoAddRequest.get();
//        if (tags != null) {
//            interfaceInfo.setTags(GSON.toJson(tags));
//        }
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
//        interfaceInfo.setFavourNum(0);
//        interfaceInfo.setThumbNum(0);
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
//        List<String> tags = interfaceInfoUpdateRequest.getTags();
//        if (tags != null) {
//            interfaceInfo.setTags(GSON.toJson(tags));
//        }
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
    @GetMapping("/get")

    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 管理员上线接口
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BeanUtils.copyProperties(idRequest, interfaceInfo);

        //判断接口是否可以调用
        com.hc.hcapiclientsdk.model.User user = new com.hc.hcapiclientsdk.model.User();
        user.setUserName("test");
        String userNameByPost = hcApiClient.getUserNameByPost(user);
        if(StringUtils.isBlank(userNameByPost)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }

        //仅本人或者管理员可修改
        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
        newInterfaceInfo.setId(id);
        newInterfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);

//        User loginUser = userService.getLoginUser(request);
//        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.updateById(newInterfaceInfo);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(result);
    }


    /**
     * 下线
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody  IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BeanUtils.copyProperties(idRequest, interfaceInfo);

        //判断接口是否可以调用
        com.hc.hcapiclientsdk.model.User user = new com.hc.hcapiclientsdk.model.User();
        user.setUserName("test");
        String userNameByPost = hcApiClient.getUserNameByPost(user);
        if(StringUtils.isBlank(userNameByPost)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }

        //仅本人或者管理员可修改
        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
        newInterfaceInfo.setId(id);
        newInterfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);

//        User loginUser = userService.getLoginUser(request);
//        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.updateById(newInterfaceInfo);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        long newInterfaceInfoId = interfaceInfo.getId();
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
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId()<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();

        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        //判断接口状态以及是否存在
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(interfaceInfo.getStatus()==InterfaceInfoStatusEnum.OFFLINE.getValue()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口已关闭");
        }

        //判断当前用户身份信息
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        HcApiClient tempHcApiClient=new HcApiClient(accessKey,secretKey);
        Gson gson = new Gson();

        //判断接口是否可以调用
        com.hc.hcapiclientsdk.model.User user = new com.hc.hcapiclientsdk.model.User();
        user=gson.fromJson(userRequestParams,com.hc.hcapiclientsdk.model.User.class);

        String userNameByPost = tempHcApiClient.getUserNameByPost(user);

//        //仅本人或者管理员可修改
//        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
//        newInterfaceInfo.setId(id);
//        newInterfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
//        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);


        return ResultUtils.success(userNameByPost);
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
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo( InterfaceInfoQueryRequest interfaceInfoQueryRequest) {

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
     * 分页获取当前用户创建的资源列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
//            HttpServletRequest request) {
//        if (interfaceInfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        interfaceInfoQueryRequest.setUserId(loginUser.getId());
//        long current = interfaceInfoQueryRequest.getCurrent();
//        long size = interfaceInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
//                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
//        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
//    }

    // endregion






    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param
     * @return
     */
    @PostMapping("/list/page/")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if(interfaceInfoQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest,interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        String description = interfaceInfoQuery.getDescription();

        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description),"description",description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current,size),queryWrapper);
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
