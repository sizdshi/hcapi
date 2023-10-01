package com.hc.apiproject.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.apiproject.model.dto.user.UserBindEmailRequest;
import com.hc.apiproject.model.dto.user.UserEmailLoginRequest;
import com.hc.apiproject.model.dto.user.UserEmailRegisterRequest;
import com.hc.apiproject.model.dto.user.UserUnBindEmailRequest;
import com.hc.hcapicommon.model.dto.user.UserQueryRequest;
import com.hc.hcapicommon.model.dto.user.UserRegisterRequest;
import com.hc.hcapicommon.model.entity.User;
import com.hc.hcapicommon.model.vo.LoginUserVO;
import com.hc.hcapicommon.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);


    /**
     * 用户电子邮件注册
     *
     * @param userEmailRegisterRequest 用户电子邮件注册请求
     * @return long
     */
    long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 是游客
     *
     * @param request 要求
     * @return {@link User}
     */
    User isTourist(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 更新 secretKey
     *
     * @param id 用户id
     * @return boolean
     */
    boolean updateSecretKey(Long id);

    /**
     * 更新凭证
     * 凭证
     *
     * @param loginUser 登录用户
     * @return {@link UserVO}
     */
    UserVO updateCertificate(User loginUser);

    /**
     * 添加钱包余额
     *
     * @param userId    用户id
     * @param addPoints 添加点
     * @return boolean
     */
    boolean addWalletBalance(Long userId, Integer addPoints);

    /**
     * 减少钱包余额
     *
     * @param userId      用户id
     * @param reduceScore 减少分数
     * @return boolean
     */
    boolean reduceWalletBalance(Long userId, Integer reduceScore);

    /**
     * 用户电子邮件登录
     *
     * @param userEmailLoginRequest 用户电子邮件登录请求
     * @param request               要求
     * @return {@link UserVO}
     */
    UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 用户绑定电子邮件
     *
     * @param userEmailLoginRequest 用户电子邮件登录请求
     * @param request               要求
     * @return {@link UserVO}
     */
    UserVO userBindEmail(UserBindEmailRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 用户取消绑定电子邮件
     *
     * @param request                要求
     * @param userUnBindEmailRequest 用户取消绑定电子邮件请求
     * @return {@link UserVO}
     */
    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request);


}
