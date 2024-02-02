package com.rick.admin.auth.config;

import com.rick.admin.auth.authentication.AdminExpiredSessionStrategy;
import com.rick.admin.auth.validate.image.ValidateFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.annotation.Resource;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:40
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled=true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private ValidateFilter validateFilter;

    @Resource
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Resource
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Resource
    private LogoutSuccessHandler logoutSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().disable()
                .authorizeRequests()
                .antMatchers("/forbidden", "/kaptcha", "/login").permitAll()
                .and()
                .authorizeRequests()
                // 第三方jar中的配置接口权限， 否则通过注解配置
                .antMatchers("/reports/695981455636959232", "/reports/695981455636959232/export", "/forms/page/695978675677433856/**").hasAnyAuthority("mm_material_read")
                .antMatchers(HttpMethod.POST,"/forms/ajax/695978675677433856").hasAnyAuthority("mm_material_add")
                .antMatchers(HttpMethod.PUT,"/forms/ajax/695978675677433856/**").hasAnyAuthority("mm_material_edit")
                .antMatchers(HttpMethod.DELETE,"/forms/ajax/695978675677433856/**").hasAnyAuthority("mm_material_delete")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .permitAll()
                .and()
                .csrf().disable()// csrf会拦截POST请求 https://www.jianshu.com/p/2c275c75c77a
                .sessionManagement().
                invalidSessionUrl("/login")
                .maximumSessions(1)
                .expiredSessionStrategy(new AdminExpiredSessionStrategy())
                .sessionRegistry(sessionRegistry());


        http.addFilterBefore(validateFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        //解决静态资源被拦截的问题
        web.ignoring().antMatchers("/coreui/**", "/css/**", "/js/**", "/img/**", "/plugins/**", "/favicon.ico");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
