package com.hc.apiproject.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.hc.apiproject.exception.BusinessException;
import com.hc.apiproject.exception.ThrowUtils;
import com.hc.apiproject.manager.CosManager;
import com.hc.apiproject.service.UserService;
import com.hc.hcapicommon.common.BaseResponse;
import com.hc.hcapicommon.common.ErrorCode;
import com.hc.hcapicommon.common.ResultUtils;
import com.hc.hcapicommon.model.entity.User;
import com.hc.hcapicommon.model.enums.FileUploadBizEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

//    /**
//     * 文件上传
//     *
//     * @param multipartFile
//     * @param uploadFileRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/upload")
//    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
//                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
//        String biz = uploadFileRequest.getBiz();
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//
//        validFile(multipartFile, fileUploadBizEnum);
//
//        User loginUser = userService.getLoginUser(request);
//
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
//
//        File file = null;
//        try {
//            // 上传文件
//            file = File.createTempFile(filepath, null);
//            multipartFile.transferTo(file);
//            cosManager.putObject(filepath, file);
//            // 返回可访问地址
//            return ResultUtils.success(FileConstant.COS_HOST + filepath);
//        } catch (Exception e) {
//            log.error("file upload error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
//                    log.error("file delete error, filepath = {}", filepath);
//                }
//            }
//        }
//    }

    /**
     * 更换头像
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<Boolean> updateMyAvatar(MultipartFile multipartFile,
                                                HttpServletRequest request) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 1.上传图片到服务器
        File file = null;
        String url;

        try {
            // 获取文件的后缀
            String contentType = multipartFile.getContentType();
            ThrowUtils.throwIf(contentType == null, ErrorCode.SYSTEM_ERROR);
            String suffix = "." + contentType.substring(contentType.lastIndexOf("/") + 1);
            // 生成文件
            file = new File(IdUtil.simpleUUID() + suffix);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(multipartFile.getBytes());
            outputStream.close();
            // 上传文件
            url = cosManager.upLoad("", file);
            if ("".equals(url)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.info("删除文件 {} 失败", file.getName());
                }
            }
        }
        // 2.更新数据库用户头像
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(url);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
