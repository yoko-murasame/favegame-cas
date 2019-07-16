package cn.dmdream.cas.custom;

import cn.dmdream.cas.credential.UsernamePasswordCaptchaCredential;
import cn.dmdream.cas.exception.CapchaErrorException;
import cn.dmdream.cas.exception.MyAccountNotFoundException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @description: 自定义验证器
 */
public class MyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    public MyAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(Credential credential) throws GeneralSecurityException, PreventedException {

        UsernamePasswordCaptchaCredential mycredential1 = (UsernamePasswordCaptchaCredential) credential;

        //校验验证码
        String capcha = mycredential1.getCapcha();
        //request域
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestCapcha = (String) attributes.getRequest().getSession().getAttribute("capcha");
        if (!capcha.equalsIgnoreCase(requestCapcha)) {
            throw new CapchaErrorException("验证码错误");
        }

        //连接数据库、判断是否有该用户
        DriverManagerDataSource d = new DriverManagerDataSource();
        d.setDriverClassName("com.mysql.jdbc.Driver");
        d.setUrl("jdbc:mysql://cloud.dmdream.cn:3306/favegame?useUnicode=true&characterEncoding=UTF-8");
        d.setUsername("root");
        d.setPassword("s18334435420");
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(d);

        //获取手机号码
        String phone = ((UsernamePasswordCredential) mycredential1).getUsername();
        if (phone == null || "".equals(phone)) {
            throw new MyAccountNotFoundException();
        }
        String phoneReg = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (!phone.matches(phoneReg)) {
            throw new MyAccountNotFoundException();
        }
        //查询用户
        Map<String, Object> user = template.queryForMap("select * from tab_user u left join tab_userInfo ui on u.id=ui.userId where u.gmUserPhone=?", phone);
        if (user == null) {
            throw new AccountNotFoundException();
        }

        //校验短信验证码
        //获取redis中的短信校验码
        String redisCode = (String) redisTemplate.opsForHash().get("loginSmsSet", phone);
        String password = mycredential1.getPassword();
        if (password.equalsIgnoreCase(redisCode)) {
            return createHandlerResult(mycredential1, principalFactory.createPrincipal(phone));
        } else {
            throw new FailedLoginException();
            //throw new SmsErrorException();
        }
        //返回多属性（暂时不知道怎么用，没研究）
        //Map<String, Object> map=new HashMap<>();
        //map.put("email", "3105747142@qq.com");

//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        if(encoder.matches(mycredential1.getPassword(),user.get("password").toString())){
//            return createHandlerResult(mycredential1, principalFactory.createPrincipal(username, map), null);
//        }

    }

    @Override
    public boolean supports(Credential credential) {
        return credential instanceof UsernamePasswordCaptchaCredential;
    }
}