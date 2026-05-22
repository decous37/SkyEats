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

import java.io.File;
import java.util.UUID;



@RestController
@RequestMapping("/admin/common")
@Api("tags = 通用接口")
@Slf4j
public class CommonController {
    private final AliOssUtil aliOssUtil;
    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
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

            //保存到本地路径，因为暂时没有阿里云oss账户
            String dirPath = "E:\\TempFiles\\software\\Frontend\\nginx-1.20.2\\html\\upload";            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir,fileName);
            file.transferTo(dest);

//            String filePath =aliOssUtil.upload(file.getBytes(), objectName);

            String filePath = "http://localhost/upload/" +fileName;
            return Result.success(filePath);


        }catch (Exception e){
            log.error("文件上传失败:{}", e.getMessage());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
