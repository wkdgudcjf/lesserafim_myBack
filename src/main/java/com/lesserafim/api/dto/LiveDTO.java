package com.lesserafim.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveDTO {
	private String signature;
	private String meetingNumber;
	private String password;
	private String sdkKey;
}
