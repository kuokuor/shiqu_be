package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.kuokuor.shiqu.commom.domain.R;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图片控制串
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/11 18:11
 */
@RestController
@RequestMapping("/image")
public class ImageController {

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    /**
     * 得到上传图片的凭证
     *
     * @return
     */
    @SaCheckLogin
    @GetMapping("/getToken")
    public R getToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        // 过期时间
        long expireSeconds = 60 * 60 * 10;
        return R.ok(auth.uploadToken(bucket, null, expireSeconds, null));
    }

}
