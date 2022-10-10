package com.lesserafim.api.service;

import com.lesserafim.api.entity.Member;
import com.lesserafim.api.entity.PhotoCard;

public interface AsyncService {
	public void asyncInsertMember(Member member);
	public void asyncUpdateMember(Member member);
	public void asyncInsertPhotoCard(PhotoCard photoCard);
}