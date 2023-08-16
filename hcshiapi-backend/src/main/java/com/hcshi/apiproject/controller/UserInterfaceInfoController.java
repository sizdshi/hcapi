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
import com.hcshi.apiproject.model.dto.interfaceinfo.UserInterfaceInfoAddRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.UserInterfaceInfoInvokeRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.UserInterfaceInfoQueryRequest;
import com.hcshi.apiproject.model.dto.interfaceinfo.UserInterfaceInfoUpdateRequest;
import com.hcshi.apiproject.model.entity.UserInterfaceInfo;
import com.hcshi.apiproject.model.entity.User;
import com.hcshi.apiproject.model.enums.UserInterfaceInfoStatusEnum;
import com.hcshi.apiproject.service.UserInterfaceInfoService;
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
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserUserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private HcApiClient hcApiClient;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
//        List<String> tags = userInterfaceInfoAddRequest.get();
//        if (tags != null) {
//            userInterfaceInfo.setTags(GSON.toJson(tags));
//        }
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
//        userInterfaceInfo.setFavourNum(0);
//        userInterfaceInfo.setThumbNum(0);
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param userInterfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
//        List<String> tags = userInterfaceInfoUpdateRequest.getTags();
//        if (tags != null) {
//            userInterfaceInfo.setTags(GSON.toJson(tags));
//        }
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")

    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(userInterfaceInfo);
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
    public BaseResponse<Boolean> onlineUserInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = idRequest.getId();
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        if(userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BeanUtils.copyProperties(idRequest, userInterfaceInfo);

        //判断接口是否可以调用
        com.hc.hcapiclientsdk.model.User user = new com.hc.hcapiclientsdk.model.User();
        user.setUserName("test");
        String userNameByPost = hcApiClient.getUserNameByPost(user);
        if(StringUtils.isBlank(userNameByPost)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }

        //仅本人或者管理员可修改
        UserInterfaceInfo newUserInterfaceInfo = new UserInterfaceInfo();
        newUserInterfaceInfo.setId(id);
        newUserInterfaceInfo.setStatus(UserInterfaceInfoStatusEnum.ONLINE.getValue());
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);

//        User loginUser = userService.getLoginUser(request);
//        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.updateById(newUserInterfaceInfo);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        long newUserInterfaceInfoId = userInterfaceInfo.getId();
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
    public BaseResponse<Boolean> offlineUserInterfaceInfo(@RequestBody  IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = idRequest.getId();
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        if(userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BeanUtils.copyProperties(idRequest, userInterfaceInfo);

        //判断接口是否可以调用
        com.hc.hcapiclientsdk.model.User user = new com.hc.hcapiclientsdk.model.User();
        user.setUserName("test");
        String userNameByPost = hcApiClient.getUserNameByPost(user);
        if(StringUtils.isBlank(userNameByPost)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }

        //仅本人或者管理员可修改
        UserInterfaceInfo newUserInterfaceInfo = new UserInterfaceInfo();
        newUserInterfaceInfo.setId(id);
        newUserInterfaceInfo.setStatus(UserInterfaceInfoStatusEnum.OFFLINE.getValue());
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);

//        User loginUser = userService.getLoginUser(request);
//        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.updateById(newUserInterfaceInfo);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(result);
    }

    /**
     * 模拟接口测试调用
     *
     * @param userInterfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeUserInterfaceInfo(@RequestBody UserInterfaceInfoInvokeRequest userInterfaceInfoInvokeRequest, HttpServletRequest request) {
        if (userInterfaceInfoInvokeRequest == null || userInterfaceInfoInvokeRequest.getId()<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断ID是否存在
        long id = userInterfaceInfoInvokeRequest.getId();
        String userRequestParams = userInterfaceInfoInvokeRequest.getUserRequestParams();

        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        //判断接口状态以及是否存在
        if(userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(userInterfaceInfo.getStatus()==UserInterfaceInfoStatusEnum.OFFLINE.getValue()){
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
//        UserInterfaceInfo newUserInterfaceInfo = new UserInterfaceInfo();
//        newUserInterfaceInfo.setId(id);
//        newUserInterfaceInfo.setStatus(UserInterfaceInfoStatusEnum.OFFLINE.getValue());
//        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);


        return ResultUtils.success(userNameByPost);
    }


    /**
     * 分页获取列表（封装类）
     *
     * @param userInterfaceInfoQueryRequest
     * @param
     * @return
     */
    //@AutoCheck(mustRole = "admin")
    @PostMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo( UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {

        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        // 限制爬虫
        //ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);

        return ResultUtils.success(userInterfaceInfoList);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(@RequestBody UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest,
//            HttpServletRequest request) {
//        if (userInterfaceInfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        userInterfaceInfoQueryRequest.setUserId(loginUser.getId());
//        long current = userInterfaceInfoQueryRequest.getCurrent();
//        long size = userInterfaceInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size),
//                userInterfaceInfoService.getQueryWrapper(userInterfaceInfoQueryRequest));
//        return ResultUtils.success(userInterfaceInfoService.getUserInterfaceInfoVOPage(userInterfaceInfoPage, request));
//    }

    // endregion






    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param userInterfaceInfoQueryRequest
     * @param
     * @return
     */
    @PostMapping("/list/page/")
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        if(userInterfaceInfoQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest,userInterfaceInfoQuery);
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        String description = userInterfaceInfoQuery.getDescription();

        // description 需支持模糊搜索
        userInterfaceInfoQuery.setDescription(null);

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description),"description",description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current,size),queryWrapper);
        return ResultUtils.success(userInterfaceInfoPage);
    }

    /**
     * 编辑（用户）
     *
     * @param userInterfaceInfoEditRequest
     * @param requ
     * @return
     */
//    @PostMapping("/edit")
//    public BaseResponse<Boolean> editUserInterfaceInfo(@RequestBody UserInterfaceInfoEditRequest userInterfaceInfoEditRequest, HttpServletRequest request) {
//        if (userInterfaceInfoEditRequest == null || userInterfaceInfoEditRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
//        BeanUtils.copyProperties(userInterfaceInfoEditRequest, userInterfaceInfo);
//        List<String> tags = userInterfaceInfoEditRequest.getTags();
//        if (tags != null) {
//            userInterfaceInfo.setTags(GSON.toJson(tags));
//        }
//        // 参数校验
//        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
//        User loginUser = userService.getLoginUser(request);
//        long id = userInterfaceInfoEditRequest.getId();
//        // 判断是否存在
//        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
//        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可编辑
//        if (!oldUserInterfaceInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
//        return ResultUtils.success(result);
//    }

}
