package com.lesserafim.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveTicketDTO {
	int quiz1;
    int quiz2;
    String sex;
    String name1;
    String name2;
    String birth;
}
