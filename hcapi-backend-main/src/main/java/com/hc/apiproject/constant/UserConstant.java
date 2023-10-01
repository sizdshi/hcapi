package com.hc.apiproject.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    /**
     * 盐值，混淆密码
     */
    String SALT = "hcshi";
    /**
     * ak/sk 混淆
     */
    String CREDENTIALS = "accessKey_secretKey";
}
