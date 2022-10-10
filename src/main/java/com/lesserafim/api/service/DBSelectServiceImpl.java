package com.lesserafim.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lesserafim.api.entity.Member;
import com.lesserafim.api.repository.MemberRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class DBSelectServiceImpl implements DBSelectService{

	private final MemberRepository memberRepository;
    @Transactional(readOnly = true)
    public Member selectMember(String memberKey){
        return memberRepository.findByMemberKey(memberKey);
    }
}