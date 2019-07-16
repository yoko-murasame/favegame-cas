package cn.dmdream.cas.controller;

import cn.dmdream.cas.utils.CapchaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SmsController {

    //消息队列服务器
    @Autowired
    private JmsTemplate jmsTemplate;

    @RequestMapping("sendSms/{phone}")
    public String sendSms(@PathVariable("phone") String phone) {
        if (phone == null || "".equals(phone)) {
            return "请输入正确的手机号!";
        }
        String phoneReg = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (!phone.matches(phoneReg)) {
            return "请输入正确的手机号!";
        }
        // 创建map对象
        Map<String, String> mapMessage = new HashMap<String, String>();
        // 放入消息
        // 手机号
        mapMessage.put("mobile", phone);
        //code
        int code = (int)((Math.random()*9+1)*100000);
        mapMessage.put("code", code+"");
        String jsonMap = null;
        try {
            jsonMap = new ObjectMapper().writeValueAsString(mapMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "消息处理异常";
        }

        // 给短信发送网关服务发送消息 pyg-sms
        jmsTemplate.convertAndSend("fg-sms-login-Queue",jsonMap);
        return "success";
    }


    public static final String KEY_CAPTCHA = "capcha";

    @RequestMapping("/capcha.jpg")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        // 设置相应类型,告诉浏览器输出的内容为图片
        response.setContentType("image/jpeg");
        // 不缓存此内容
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
        try {

            HttpSession session = request.getSession();

            CapchaUtil tool = new CapchaUtil();
            StringBuffer code = new StringBuffer();
            BufferedImage image = tool.genRandomCodeImage(code);
            session.removeAttribute(KEY_CAPTCHA);
            session.setAttribute(KEY_CAPTCHA, code.toString());

            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
