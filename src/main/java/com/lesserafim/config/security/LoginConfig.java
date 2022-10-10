package com.lesserafim.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lesserafim.oauth.token.LoginProvider;

@Configuration
public class LoginConfig {
    @Value("${weverse.url}")
    private String url;

    @Value("${weverse.X-BENX-CI}")
    private String ci;
    
    @Value("${weverse.X-BENX-CIK}")
    private String cik;
    
    @Value("${weverse.domain}")
    private String domain;
    
    @Bean
    public LoginProvider loginProvider() {
        return new LoginProvider(url,ci,cik,domain);
    }
}
