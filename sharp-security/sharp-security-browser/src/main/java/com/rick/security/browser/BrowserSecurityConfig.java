
package com.rick.security.browser;

import com.rick.security.core.authentication.mobile.SmsCodeAuthenticationSecurityConfig;
import com.rick.security.core.properties.SecurityProperties;
import com.rick.security.core.validate.code.config.ValidateCodeSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * 浏览器环境下安全配置主类
 * 
 * @author zhailiang
 *
 */
@Configuration
@RequiredArgsConstructor
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final SecurityProperties securityProperties;

	private final UserDetailsService userDetailsService;

	private final SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

	private final ValidateCodeSecurityConfig validateCodeSecurityConfig;

	private final SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

	private final InvalidSessionStrategy invalidSessionStrategy;

	private final LogoutSuccessHandler logoutSuccessHandler;

	private final PersistentTokenRepository persistentTokenRepository;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.apply(validateCodeSecurityConfig)
				.and()
			.apply(smsCodeAuthenticationSecurityConfig)
				.and()
			//记住我配置，如果想在'记住我'登录时记录日志，可以注册一个InteractiveAuthenticationSuccessEvent事件的监听器
			.rememberMe()
				.tokenRepository(persistentTokenRepository)
				.tokenValiditySeconds(securityProperties.getBrowser().getRememberMeSeconds())
				.userDetailsService(userDetailsService)
				.and()
			.sessionManagement()
				.invalidSessionStrategy(invalidSessionStrategy)
				.maximumSessions(securityProperties.getBrowser().getSession().getMaximumSessions())
				.maxSessionsPreventsLogin(securityProperties.getBrowser().getSession().isMaxSessionsPreventsLogin())
				.expiredSessionStrategy(sessionInformationExpiredStrategy)
				.and()
				.and()
			.logout()
				.logoutUrl("/signOut")
				.logoutSuccessHandler(logoutSuccessHandler)
				.deleteCookies("JSESSIONID")
				.and()
			.csrf().disable();
	}
}
