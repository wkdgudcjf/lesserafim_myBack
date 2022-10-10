package com.lesserafim.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lesserafim.api.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
	@Transactional(readOnly = true)
	Member findByMemberKey(String memberKey);
}