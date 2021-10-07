
package com.rick.security.core.validate.code.handler;

import com.rick.security.core.validate.code.ValidateCode;
import com.rick.security.core.validate.code.ValidateCodeTypeEnum;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 校验码存取器
 * 
 * @author zhailiang
 *
 */
public interface ValidateCodeRepository {

	/**
	 * 保存验证码
	 * @param request
	 * @param code
	 * @param validateCodeType
	 */
	void save(ServletWebRequest request, ValidateCode code, ValidateCodeTypeEnum validateCodeType);
	/**
	 * 获取验证码
	 * @param request
	 * @param validateCodeType
	 * @return
	 */
	ValidateCode get(ServletWebRequest request, ValidateCodeTypeEnum validateCodeType);
	/**
	 * 移除验证码
	 * @param request
	 * @param codeType
	 */
	void remove(ServletWebRequest request, ValidateCodeTypeEnum codeType);

}
