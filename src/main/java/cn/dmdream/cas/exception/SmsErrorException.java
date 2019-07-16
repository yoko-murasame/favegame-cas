package cn.dmdream.cas.exception;

import javax.security.auth.login.AccountException;

/**
 * 短信验证码错误异常
 */
public class SmsErrorException extends AccountException {

    public SmsErrorException() {
        super();
    }

    public SmsErrorException(String msg) {
        super(msg);
    }

}
