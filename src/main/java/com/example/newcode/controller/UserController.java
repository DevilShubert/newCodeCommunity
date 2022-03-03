package com.example.newcode.controller;

import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Value("${community.path.domain}")
    String domainPath;

    @Value("${community.path.upload}")
    String uploadPath;

    @Value("${server.servlet.context-path}")
    String context;


    @RequestMapping("/setting")
    @LoginRequired
    public String settingPage(){
        return "/site/setting";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @LoginRequired
    public String doSetting(MultipartFile imageFile, Model model){
        // 1: get file and suffix
        if (imageFile == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        String fileName = imageFile.getOriginalFilename();
        // get file extension
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 2: create random fileName
        fileName = CommunityUtils.getRandomUUID() + suffix;

        // 3: store to local path
        File upload = new File(uploadPath + "/" + fileName);
        try {
            // store the image
            imageFile.transferTo(upload);
        } catch (IOException e) {
            log.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 4: update user OBJ and user table
        User user = hostHolder.getUser();
        // http://localhost:85/community/user/header/random-fileName
        String newHeaderURL = domainPath + context + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), newHeaderURL, user);
        // user.setHeaderUrl(newHeaderURL);       later update in interceptor

        return "redirect:/indexPage";
    }

    @RequestMapping(value = "header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 1: get localPath of Image
        fileName = uploadPath + "/" + fileName;
        // get suffix
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);

        // 2: use stream to return image to front
        try(
                FileInputStream inputStream = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(value = "updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model){
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPwdMsg","原始密码不为空");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        String salt = user.getSalt();
        oldPassword = CommunityUtils.md5(oldPassword + salt);
        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("oldPwdMsg","原始密码错误");
            return "/site/setting";
        }

        if (StringUtils.isBlank(newPassword) || StringUtils.isBlank(confirmPassword)) {
            model.addAttribute("newPwdMsg", "新设置的密码不能为空");
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("newPwdMsg", "两次密码不相等");
            return "/site/setting";
        }

        // same salt
        newPassword = CommunityUtils.md5(newPassword + salt);

        userService.updatePassword(user.getId(), newPassword, user);
        return "redirect:/indexPage";
    }
}
