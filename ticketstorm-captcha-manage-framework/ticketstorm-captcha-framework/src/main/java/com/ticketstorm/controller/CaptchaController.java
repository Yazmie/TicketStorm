/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.ticketstorm.controller;

import com.ticketstorm.captcha.model.common.ResponseModel;
import com.ticketstorm.captcha.model.vo.CaptchaVO;
import com.ticketstorm.captcha.service.CaptchaService;
import com.ticketstorm.captcha.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        assert request.getRemoteHost()!=null;
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.get(data);
    }

    /**
     *
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.check(data);
    }

    /**
     *
     * @param request
     * @return
     */
    public static String getRemoteId(HttpServletRequest request) {
        String xForward = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xForward);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    /**
     *
     * @param xfwd
     * @return
     */
    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }

}
