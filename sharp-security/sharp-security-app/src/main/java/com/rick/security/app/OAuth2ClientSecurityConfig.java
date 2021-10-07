package com.rick.security.app;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.DefaultJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * @author Rick
 * @createdAt 2021-09-17 10:31:00
 */
//@EnableWebSecurity
public class OAuth2ClientSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .oauth2Client(oauth2 -> oauth2
//                    .clientRegistrationRepository(clientRegistrationRepository())
//                    .authorizedClientRepository(getOAuth2AuthorizedClientRepository())
//                    .authorizedClientService(getOAuth2AuthorizedClientService())
                    .authorizationCodeGrant(codeGrant -> codeGrant
                            .authorizationRequestRepository(getAuthorizationRequestRepository())
//                            .authorizationRequestResolver(getOAuth2AuthorizationRequestResolver())
                            .accessTokenResponseClient(getOAuth2AccessTokenResponseClient())
                    )
            );
    }

    // 授权客户端服务
//    @Bean
//    public InMemoryOAuth2AuthorizedClientService getOAuth2AuthorizedClientService() {
//        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
//    }

//    @Bean
//    public ClientRegistrationRepository clientRegistrationRepository() {
//        ClientRegistration registration = ClientRegistration.withRegistrationId("app")
//                .clientId("devyean").clientSecret("123456")
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // 值支持CODO和IMPLICIT两种模式
//                .tokenUri("https://www.baidu.com")
//                .redirectUri("https://www.baidu.com")
//                .authorizationUri("https://www.google.com")
//                .build();
//        return new InMemoryClientRegistrationRepository(registration);
//    }

//    @Bean
//    public OAuth2AuthorizedClientRepository getOAuth2AuthorizedClientRepository() {
//        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(getOAuth2AuthorizedClientService());
//    }

    @Bean
    public AuthorizationRequestRepository getAuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

//    @Bean
//    public OAuth2AuthorizationRequestResolver getOAuth2AuthorizationRequestResolver() {
//        return new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository(), "base_url");
//    }

    @Bean
    public OAuth2AccessTokenResponseClient getOAuth2AccessTokenResponseClient() {
        return new DefaultJwtBearerTokenResponseClient();
    }

    //
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .password()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
