package com.ticketstorm.service;

import com.ticketstorm.captcha.model.common.ResponseModel;
import com.ticketstorm.captcha.model.vo.CaptchaVO;
import com.ticketstorm.captcha.service.CaptchaService;
import com.ticketstorm.util.RemoteUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;


@AllArgsConstructor
public class CaptchaHandle {
    
    private final CaptchaService captchaService;
    
    public ResponseModel getCaptcha(CaptchaVO captchaVO) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        captchaVO.setBrowserInfo(RemoteUtil.getRemoteId(request));
        return captchaService.get(captchaVO);
    }
    
    public ResponseModel checkCaptcha(CaptchaVO captchaVO) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        captchaVO.setBrowserInfo(RemoteUtil.getRemoteId(request));
        return captchaService.check(captchaVO);
    }
    
    public ResponseModel verification(CaptchaVO captchaVO) {
        return captchaService.verification(captchaVO);
    }
}
