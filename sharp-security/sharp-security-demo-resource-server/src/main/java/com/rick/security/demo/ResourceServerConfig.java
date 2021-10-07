package com.rick.security.demo;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

/**
 * @author Rick
 * @createdAt 2021-09-24 16:33:00
 */
@EnableWebSecurity
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/messages/**")
                .authorizeRequests()
                .mvcMatchers("/messages/**").access("hasAuthority('SCOPE_message.read')")
                .and()
                .oauth2ResourceServer()
                .jwt();
    }
    // @formatter:on

//    @Bean
    public JwtDecoder jwtDecoder(RestTemplateBuilder builder) {
        // 授权服务器 jwk 的信息
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKey() {
            @Override
            public String getAlgorithm() {
                return "HS256";
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return "6MNSobBRCHGIO0fS6MNSobBRCHGIO0fS".getBytes();
            }
        }) // withJwkSetUri("http://qq.com:8080/oauth2/jwks")
                // 设置获取 jwk 信息的超时时间
//                .restOperations(
//                        builder.setReadTimeout(Duration.ofSeconds(3))
//                                .setConnectTimeout(Duration.ofSeconds(3))
//                                .build()
//                )
                .build();
        // 对jwt进行校验
        decoder.setJwtValidator(new OAuth2TokenValidator<Jwt>() {

            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                try {
                    SignedJWT signedJWT = SignedJWT.parse(jwt.toString());
                    JWSVerifier verifier = new MACVerifier("6MNSobBRCHGIO0fS6MNSobBRCHGIO0fS".getBytes());
                    //校验是否有效
                    if (!signedJWT.verify(verifier)) {
//                        throw ResultException.of(-1, "Token 无效");
                    }

                    //校验超时
                    Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                    if (new Date().after(expirationTime)) {
                        // throw ResultException.of(-2, "Token 已过期");
                    }

//                    //获取载体中的数据
//                    Object account = jwt.getJWTClaimsSet().getClaim("ACCOUNT");
//                    //是否有openUid
//                    if (Objects.isNull(account)){
//                        throw ResultException.of(-3, "账号为空");
//                    }
                    return OAuth2TokenValidatorResult.success();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JOSEException e) {
                    e.printStackTrace();
                }

                return OAuth2TokenValidatorResult.failure();
            }
        });
        // 对 jwt 的 claim 中增加值
        decoder.setClaimSetConverter(
                MappedJwtClaimSetConverter.withDefaults(Collections.singletonMap("为claim中增加key", custom -> "值"))
        );

        return decoder;
    }

}
