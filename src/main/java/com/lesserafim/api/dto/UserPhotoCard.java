package com.lesserafim.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPhotoCard {
    private int girlId;
    private int imageId;
    private int audioId;
    private int messageId;
    private String frontUrl;
    private String backUrl;
}
