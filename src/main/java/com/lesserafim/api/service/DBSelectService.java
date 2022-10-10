package com.lesserafim.api.service;

import org.springframework.transaction.annotation.Transactional;

import com.lesserafim.api.entity.Member;

public interface DBSelectService {
	  @Transactional(readOnly = true)
	  public Member selectMember(String memberKey);
}