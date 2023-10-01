package com.hc.apiproject.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.apiproject.exception.BusinessException;
import com.hc.apiproject.mapper.UserMapper;
import com.hc.apiproject.model.dto.user.UserBindEmailRequest;
import com.hc.apiproject.model.dto.user.UserEmailLoginRequest;
import com.hc.apiproject.model.dto.user.UserEmailRegisterRequest;
import com.hc.apiproject.model.dto.user.UserUnBindEmailRequest;
import com.hc.apiproject.service.UserService;
import com.hc.apiproject.utils.RedissonLockUtil;
import com.hc.apiproject.utils.SqlUtils;
import com.hc.hcapicommon.common.ErrorCode;
import com.hc.hcapicommon.constant.CommonConstant;
import com.hc.hcapicommon.model.dto.user.UserQueryRequest;
import com.hc.hcapicommon.model.dto.user.UserRegisterRequest;
import com.hc.hcapicommon.model.entity.User;
import com.hc.hcapicommon.model.vo.LoginUserVO;
import com.hc.hcapicommon.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hc.apiproject.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.hc.apiproject.constant.UserConstant.*;


/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedissonLockUtil redissonLockUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userName = userRegisterRequest.getUserName();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String invitationCode = userRegisterRequest.getInvitationCode();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过长");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 匹配由数字、小写字母、大写字母组成的字符串,且字符串的长度至少为1个字符
        String pattern = "[0-9a-zA-Z]+";
        if (!userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号由数字、小写字母、大写字母组成");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }


        String redissonLock = ("userRegister_" + userAccount).intern();
        return redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            User invitationCodeUser = null;
            if (StringUtils.isNotBlank(invitationCode)) {
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userLambdaQueryWrapper.eq(User::getInvitationCode, invitationCode);
                // 可能出现重复invitationCode,查出的不是一条
                invitationCodeUser = this.getOne(userLambdaQueryWrapper);
                if (invitationCodeUser == null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邀请码无效");
                }
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            // ak/sk
            String accessKey = DigestUtils.md5DigestAsHex((userAccount + SALT + CREDENTIALS).getBytes());
            String secretKey = DigestUtils.md5DigestAsHex((SALT + CREDENTIALS + userAccount).getBytes());

            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(userName);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setUserAvatar("https://picture-1254371329.cos.ap-nanjing.myqcloud.com/144234cd7686cdc9cc844dad6abae1b.jpg?q-sign-algorithm=sha1&q-ak=AKID9a7QlZtiE-GmPTUzsLrabR7SEsv4ggbdIEUBQ5GjvAXF70d12YqOwMzGBhGwN0D2&q-sign-time=1696173100;1696176700&q-key-time=1696173100;1696176700&q-header-list=host&q-url-param-list=ci-process&q-signature=e2a9d8d2b6fc57aa4a73523d4c6b85ebb2d3e621&x-cos-security-token=DjtxNBcCZ4RxQO8ij4y3MxMyiMBBcNya0d3dec6595ab48e61d4e231c3c7d06ccjEFnw3iKvniTs-CVyl3gNkuKzKFAAHbsXn6s848J77O7b41yQuQ3SarPzELcw8pkGzw-CkMKn2W4Yf-1PkqT3UONEH_x8qv1QuKdQM5QNx0Ik5_hzdwyU6259z17LMAj7LhmQkL7LB7C5ABCDIDzpCKdkiI76J5ZJTU0XK4EoI07rgfAX3q5-N7siL1WVaok&ci-process=originImage");
            if (invitationCodeUser != null) {
                user.setBalance(100);
                this.addWalletBalance(invitationCodeUser.getId(), 100);
            }
            user.setInvitationCode(RandomUtil.randomString(8));
            //判断数据是否插入
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }, "注册账号失败");


    }

    @Override
    public long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest) {
        String emailAccount = userEmailRegisterRequest.getEmailAccount();
        String captcha = userEmailRegisterRequest.getCaptcha();
        String userName = userEmailRegisterRequest.getUserName();
        String invitationCode = userEmailRegisterRequest.getInvitationCode();
        String agreeToAnAgreement = userEmailRegisterRequest.getAgreeToAnAgreement();

        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userName.length() > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过长");
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }

        String redissonLock = ("userAccount" + emailAccount).intern();
        return redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("userAccount", emailAccount);
            long count = userMapper.selectCount(userQueryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            User invitationCodeUser = null;
            if (StringUtils.isNotBlank(invitationCode)) {
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userLambdaQueryWrapper.eq(User::getInvitationCode, invitationCode);
                invitationCodeUser = this.getOne(userLambdaQueryWrapper);
                if (invitationCodeUser == null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邀请码无效");
                }
            }


            // ak/sk
            String accessKey = DigestUtils.md5DigestAsHex((Arrays.toString(RandomUtil.randomBytes(10)) + SALT + CREDENTIALS).getBytes());
            String secretKey = DigestUtils.md5DigestAsHex((SALT + CREDENTIALS + Arrays.toString(RandomUtil.randomBytes(10))).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(emailAccount);
            user.setUserName(userName);
            user.setAccessKey(accessKey);
            user.setEmail(emailAccount);
            user.setSecretKey(secretKey);
            if (invitationCodeUser != null) {
                user.setBalance(100);
                this.addWalletBalance(invitationCodeUser.getId(), 100);
            }
            user.setInvitationCode(RandomUtil.randomString(8));
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }, "邮箱账号注册失败");

    }

//    @Override
//    public long userRegister(String userAccount, String userPassword, String checkPassword) {
//        // 1. 校验
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
//        }
//        if (userAccount.length() < 4) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
//        }
//        if (userPassword.length() < 8 || checkPassword.length() < 8) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
//        }
//        // 密码和校验密码相同
//        if (!userPassword.equals(checkPassword)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
//        }
//        synchronized (userAccount.intern()) {
//            // 账户不能重复
//            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("userAccount", userAccount);
//            long count = this.baseMapper.selectCount(queryWrapper);
//            if (count > 0) {
//                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
//            }
//            // 2. 加密
//            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
//            // 3.分配ak,sk
//            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
//            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
//
//            // 4. 插入数据
//            User user = new User();
//            user.setUserAccount(userAccount);
//            user.setUserRole("user");
//            //user.setUserProfile("游客");
//            user.setUserPassword(encryptPassword);
//            user.setAccessKey(accessKey);
//            user.setSecretKey(secretKey);
//            user.setUserAvatar("https://image-bed-ichensw.oss-cn-hangzhou.aliyuncs.com/006VVqOWgy1h43uaynbyxj30go0go0u7.jpg");
//
//            boolean saveResult = this.save(user);
//            if (!saveResult) {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
//            }
//            return user.getId();
//        }
//    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public boolean isAdmin(User user) {
        return false;
    }

    @Override
    public User isTourist(HttpServletRequest request) {
        return null;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }



    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateSecretKey(Long id) {
        User user = this.getById(id);
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        user.setSecretKey(secretKey);
        user.setAccessKey(accessKey);
        return this.updateById(user);
    }

    @Override
    public UserVO updateCertificate(User loginUser) {
        return null;
    }

    @Override
    public boolean addWalletBalance(Long userId, Integer addPoints) {
        return false;
    }

    @Override
    public boolean reduceWalletBalance(Long userId, Integer reduceScore) {
        return false;
    }

    @Override
    public UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public UserVO userBindEmail(UserBindEmailRequest userEmailLoginRequest, HttpServletRequest request) {
        String emailAccount = userEmailLoginRequest.getEmailAccount();
        String captcha = userEmailLoginRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 查询用户是否绑定该邮箱
        UserVO loginUser = this.getLoginUser(request);
        if (loginUser.getEmail() != null && emailAccount.equals(loginUser.getEmail())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已绑定此邮箱,请更换新的邮箱！");
        }
        // 查询邮箱是否已经绑定
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = this.getOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此邮箱已被绑定,请更换新的邮箱！");
        }
        user = new User();
        user.setId(loginUser.getId());
        user.setEmail(emailAccount);
        boolean bindEmailResult = this.updateById(user);
        if (!bindEmailResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱绑定失败,请稍后再试！");
        }
        loginUser.setEmail(emailAccount);
        return loginUser;
        return null;
    }

    @Override
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {


        return null;
    }


}
