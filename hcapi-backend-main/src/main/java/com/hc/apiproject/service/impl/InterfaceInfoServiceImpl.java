package com.hc.apiproject.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.apiproject.constant.CommonConstant;
import com.hc.apiproject.exception.BusinessException;
import com.hc.apiproject.exception.ThrowUtils;
import com.hc.apiproject.mapper.InterfaceInfoMapper;
import com.hc.apiproject.service.InterfaceInfoService;
import com.hc.apiproject.service.UserInterfaceInfoService;
import com.hc.apiproject.service.UserService;
import com.hc.apiproject.utils.SqlUtils;
import com.hc.hcapiclientsdk.client.HcApiClient;
import com.hc.hcapicommon.common.ErrorCode;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.hc.hcapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.hc.hcapicommon.model.entity.InterfaceInfo;
import com.hc.hcapicommon.model.entity.User;
import com.hc.hcapicommon.model.entity.UserInterfaceInfo;
import com.hc.hcapicommon.model.vo.InterfaceInfoVO;
import com.hc.hcapicommon.model.vo.RequestParamsRemarkVO;
import com.hc.hcapicommon.model.vo.ResponseParamsRemarkVO;
import com.hc.hcapicommon.model.vo.UserVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sizd-shi
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-07-10 16:47:40
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Resource
    private UserService userService;


    private final UserInterfaceInfoService userInterfaceInfoService;

    public InterfaceInfoServiceImpl(UserInterfaceInfoService userInterfaceInfoService) {
        this.userInterfaceInfoService = userInterfaceInfoService;
    }


    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = interfaceInfo.getId();
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        Integer status = interfaceInfo.getStatus();
        String method = interfaceInfo.getMethod();
        Long userId = interfaceInfo.getUserId();
        Date createTime = interfaceInfo.getCreateTime();
        Date updateTime = interfaceInfo.getUpdateTime();
        Integer isDelete = interfaceInfo.getIsDelete();


        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, url), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(name) && name.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }


    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }

        String searchText = interfaceInfoQueryRequest.getSearchText();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        Long id = interfaceInfoQueryRequest.getId();
        Long userId = interfaceInfoQueryRequest.getUserId();
        long current = interfaceInfoQueryRequest.getCurrent();
        long pageSize = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        Date createTime = interfaceInfoQueryRequest.getCreateTime();

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("description", searchText).or().like("name", searchText);
        }

        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.gt(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    /**
     * 向前端展示分页信息
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();

        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent()
                , interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }
        User loginUser = userService.getLoginUser(request);

        Set<Long> userIdSet = interfaceInfoList.stream().map(InterfaceInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(
                interfaceInfo -> {
                    //将interfaceInfo信息转为前端interfaceInfoVO
                    InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
                    Long userId = interfaceInfo.getUserId();

                    UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                            .eq(UserInterfaceInfo::getUserId, loginUser.getId())
                            .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfo.getId())
                            .one();

                    // 判断是否是当前用户拥有的接口
                    boolean isOwnedByUser = false;

                    if (userInterfaceInfo != null) {
                        isOwnedByUser = true;
                        interfaceInfoVO.setTotalNum(userInterfaceInfo.getTotalNum());
                        interfaceInfoVO.setLeftNum(userInterfaceInfo.getLeftNum());
                    }

                    User user = userIdListMap.getOrDefault(userId, Collections.emptyList()).stream().findFirst().orElse(null);
                    interfaceInfoVO.setUser(userService.getUserVO(user));
                    // 封装请求参数说明和响应参数说明
                    List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
                    List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);
                    interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
                    interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);
                    // 设置是否为当前用户拥有的接口
                    interfaceInfoVO.setIsOwnerByCurrentUser(isOwnedByUser);

                    return interfaceInfoVO;

                }).collect(Collectors.toList());

        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

    /**
     * 根据用户ID得到列表请求
     *
     * @param interfaceInfoPage 接口信息分页
     * @param request           当前会话
     * @return
     */
    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOByUserIdPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoListByPage = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVoPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize()
                , interfaceInfoPage.getTotal());

        if (CollectionUtils.isEmpty(interfaceInfoListByPage)) {
            return interfaceInfoVoPage;
        }

        User user = userService.getLoginUser(request);
        Long userId = user.getId();

        //过滤出当前用户拥有的接口
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoListByPage.stream()
                .map(interfaceInfo -> {
                    InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
                    UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                            .eq(UserInterfaceInfo::getUserId, userId)
                            .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfo.getId())
                            .one();
                    if (userInterfaceInfo != null) {
                        interfaceInfoVO.setTotalNum(userInterfaceInfo.getTotalNum());
                        interfaceInfoVO.setLeftNum(userInterfaceInfo.getLeftNum());
                        // 封装请求参数说明和响应参数说明
                        List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
                        List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);
                        interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
                        interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);
                        return interfaceInfoVO;
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());


        interfaceInfoVoPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVoPage;
    }

    @Override
    public HcApiClient getHcApiClient(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        return new HcApiClient(accessKey, secretKey);
    }

    @Override
    public boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getResponseParamsRemark()));

        // 参数校验
        this.validInterfaceInfo(interfaceInfo, false);
        return this.updateById(interfaceInfo);
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
        // 1. 关联查询用户信息
        Long userId = interfaceInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        interfaceInfoVO.setUser(userVO);
        // 封装请求参数说明 和 响应参数说明
        List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
        List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);
        interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
        interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);
        return interfaceInfoVO;
    }


}




