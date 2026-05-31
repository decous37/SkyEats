package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sky.properties.UploadProperties;

import java.io.File;
import java.util.UUID;



@RestController
@RequestMapping("/admin/common")
@Api("tags = 通用接口")
@Slf4j
public class CommonController {
    private final AliOssUtil aliOssUtil;
    private final UploadProperties uploadProperties;

    public CommonController(AliOssUtil aliOssUtil, UploadProperties uploadProperties)
    {
        this.aliOssUtil = aliOssUtil;
        this.uploadProperties = uploadProperties;
    }

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}", file);

        try{
            String originalFilename = file.getOriginalFilename();

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            String fileName = UUID.randomUUID().toString() + extension;
//            String objectName = UUID.randomUUID().toString() + extension;

            String dirPath = uploadProperties.getDirPath();
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir,fileName);
            file.transferTo(dest);

//            String filePath =aliOssUtil.upload(file.getBytes(), objectName);

            String filePath = uploadProperties.getAccessUrlPrefix() + fileName;
            return Result.success(filePath);


        }catch (Exception e){
            log.error("文件上传失败:{}", e.getMessage());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
