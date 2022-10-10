package com.lesserafim.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeverseDTO {
	private String userKey;
	private String userId;
	private boolean isValidated;
}
