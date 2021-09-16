
package com.rick.security.browser.validate.code.impl;

import com.rick.security.core.validate.code.ValidateCode;
import com.rick.security.core.validate.code.ValidateCodeRepository;
import com.rick.security.core.validate.code.ValidateCodeType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 基于session的验证码存取器
 *
 * @author zhailiang
 *
 */
@Component
public class SessionValidateCodeRepository implements ValidateCodeRepository {

	/**
	 * 验证码放入session时的前缀
	 */
	String SESSION_KEY_PREFIX = "SESSION_KEY_FOR_CODE_";
	
	@Override
	public void save(ServletWebRequest request, ValidateCode code, ValidateCodeType validateCodeType) {
		request.getRequest().getSession().setAttribute(getSessionKey(validateCodeType), code);
	}
	
	/**
	 * 构建验证码放入session时的key
	 * 
	 * @return
	 */
	private String getSessionKey(ValidateCodeType validateCodeType) {
		return SESSION_KEY_PREFIX + validateCodeType.toString().toUpperCase();
	}

	@Override
	public ValidateCode get(ServletWebRequest request, ValidateCodeType validateCodeType) {
		return (ValidateCode) request.getRequest().getSession().getAttribute(getSessionKey(validateCodeType));
	}

	@Override
	public void remove(ServletWebRequest request, ValidateCodeType codeType) {
		request.getRequest().getSession().removeAttribute(getSessionKey(codeType));
	}

}
