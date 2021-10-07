
package com.rick.security.core.validate.code.config;

import com.rick.security.core.properties.SecurityProperties;
import com.rick.security.core.validate.code.handler.ValidateCodeGenerator;
import com.rick.security.core.validate.code.image.ImageCodeGenerator;
import com.rick.security.core.validate.code.sms.SmsCodeSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 验证码相关的扩展点配置。配置在这里的bean，业务系统都可以通过声明同类型或同名的bean来覆盖安全
 * 模块默认的配置。
 * 
 * @author zhailiang
 *
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ValidateCodeBeanConfig {
	
	private final SecurityProperties securityProperties;
	
	/**
	 * 图片验证码图片生成器
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(name = "imageValidateCodeGenerator")
	public ValidateCodeGenerator imageValidateCodeGenerator() {
		ImageCodeGenerator codeGenerator = new ImageCodeGenerator(); 
		codeGenerator.setSecurityProperties(securityProperties);
		return codeGenerator;
	}
	
	/**
	 * 短信验证码发送器
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(SmsCodeSender.class)
	public SmsCodeSender smsCodeSender() {
		return (mobile, code) -> {
			log.warn("请配置真实的短信验证码发送器(SmsCodeSender)");
			log.info("向手机"+mobile+"发送短信验证码"+code);
		};
	}

}
