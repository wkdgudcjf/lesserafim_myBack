package com.lesserafim.api.service;

import org.springframework.web.multipart.MultipartFile;

import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.common.ApiResponse;

public interface PhotoCardService
{
	public ApiResponse getUserPhotoCardList(MemberPrincial memberPrincial);
	
	public ApiResponse getAdminPhotoCardList(MemberPrincial memberPrincial);
	
	public ApiResponse getPhotoCardValidation(int girlId);

	public ApiResponse insertAdminPhotoCard(MultipartFile imageFront, MultipartFile imageBack, int girl_id, MemberPrincial memberPrincial);

	public ApiResponse deleteKey(int mId, MemberPrincial memberPrincial);
}
