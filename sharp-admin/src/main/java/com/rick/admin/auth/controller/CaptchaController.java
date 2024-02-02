package com.rick.admin.auth.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.rick.admin.auth.validate.image.CaptchaImageVO;
import com.rick.admin.common.AdminConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;

/**
 * @author rick
 */
@RestController
@RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
@RequiredArgsConstructor
public class CaptchaController {

    private final DefaultKaptcha captchaProducer;

    /**
     * 获取验证码
     */
    @GetMapping
    public void kaptcha(HttpSession session, HttpServletResponse response) throws Exception {

        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        String capText = captchaProducer.createText();
        CaptchaImageVO captchaImageVO = new CaptchaImageVO(capText, 2 * 60);
        // 将验证码存到session
        session.setAttribute(AdminConstants.KAPTCHA_SESSION_KEY, captchaImageVO);

        //将图片返回给前端
        try (ServletOutputStream out = response.getOutputStream()) {
            BufferedImage bi = captchaProducer.createImage(capText);
            ImageIO.write(bi, "jpg", out);
            out.flush();
        } //使用try-with-resources不用手动关闭流
    }

}
