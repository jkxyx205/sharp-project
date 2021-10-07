package com.rick.security.app.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Rick
 * @createdAt 2021-09-17 13:42:00
 */
@Controller
@RequiredArgsConstructor
public class OAuth2ClientController {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/test1")
    public String index() {
        ClientRegistration appRegistration =
                clientRegistrationRepository.findByRegistrationId("app");

        return "index";
    }


    @GetMapping("/test2")
    public String index(Authentication authentication) {
        OAuth2AuthorizedClient authorizedClient =
                this.authorizedClientService.loadAuthorizedClient("devyean", authentication.getName());

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        return "index";
    }
}
