package cn.dmdream.cas.credential;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;

import javax.validation.constraints.Size;

/**
 * @author: wangsaichao
 * @date: 2018/8/31
 * @description: 验证码 Credential
 */
public class UsernamePasswordCaptchaCredential extends RememberMeUsernamePasswordCredential {

    @Size(min = 6,max = 6, message = "require capcha")
    private String capcha;

    public String getCapcha() {
        return capcha;
    }

    public void setCapcha(String capcha) {
        this.capcha = capcha;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.capcha)
                .toHashCode();
    }
}