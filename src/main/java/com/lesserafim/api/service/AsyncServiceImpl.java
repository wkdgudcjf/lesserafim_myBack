package com.lesserafim.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lesserafim.api.entity.Member;
import com.lesserafim.api.entity.PhotoCard;
import com.lesserafim.api.repository.MemberRepository;
import com.lesserafim.api.repository.PhotoCardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AsyncServiceImpl implements AsyncService {
	private final MemberRepository memberRepository;
	private final PhotoCardRepository photoCardRepository;
	
	@Override
	public void asyncInsertMember(Member member) {
		memberRepository.saveAndFlush(member);
	}

	@Override
	public void asyncUpdateMember(Member member) {
		memberRepository.saveAndFlush(member);
	}

	@Override
	public void asyncInsertPhotoCard(PhotoCard photoCard) {
		photoCardRepository.saveAndFlush(photoCard);
	}
}