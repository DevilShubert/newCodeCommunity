package com.example.newcode.controller;

import com.example.newcode.service.UserLoginService;

import com.example.newcode.util.constant.CommunityConstant;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
public class LoginController implements CommunityConstant {

    @Autowired
    UserLoginService userLoginService;

    @Autowired
    DefaultKaptcha defaultKaptcha;

    @Value("${community.path.domain}")
    private String contextPath;

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public void getDefaultKaptcha(HttpServletResponse response, HttpSession session) {
        String kaptchaCode = defaultKaptcha.createText();
        log.info(kaptchaCode);
        BufferedImage image = defaultKaptcha.createImage(kaptchaCode);

        // 存入对应会话的session
        session.setAttribute("kaptcha", kaptchaCode);

        // 将突图片输出给浏览器
        response.setContentType("image/png");

        try{
            // 利用流输出给图片
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            log.error("响应验证码失败:" + e.getMessage());
        }

    }

    @RequestMapping(value = "/doLogin", method = RequestMethod.POST)
    public String doLogin(String username, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response){
        // 1: check kaptcha
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 2: check rememberMe
        long expiredSeconds = rememberme ?  REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        // 3: chechk password and insert login_ticket or not
        Map<String, Object> map = userLoginService.doLogin(username, password, expiredSeconds);
        // user corrected
        if (map.get("ticket") != null){
            // 4: store the ticket into cookie
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // set cookie useful location
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            return "redirect:/indexPage";
        } else {
            // 5: not corrected
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value = "logout" ,method = RequestMethod.GET)
    public String logout(HttpServletRequest request, Model model){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie: cookies) {
            if ("ticket".equals(cookie.getName())) {
                userLoginService.logout(cookie.getValue());
                return "redirect:/indexPage";
            }
        }
        model.addAttribute("usernameMsg","请先登录");
        return "/site/login";
    }
}
