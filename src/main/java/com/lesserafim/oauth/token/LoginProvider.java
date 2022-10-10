package com.lesserafim.oauth.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginProvider {
    private String url;
    private String ci;
    private String cik;
    private String domain;
}
