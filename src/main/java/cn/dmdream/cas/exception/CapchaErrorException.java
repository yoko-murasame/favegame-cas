package cn.dmdream.cas.exception;

import javax.security.auth.login.AccountException;

/**
 * 验证码错误异常
 */
public class CapchaErrorException extends AccountException {
    public CapchaErrorException() {
        super();
    }

    public CapchaErrorException(String msg) {
        super(msg);
    }
}
