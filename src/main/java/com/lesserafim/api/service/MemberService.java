package com.lesserafim.api.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lesserafim.common.ApiResponse;
import com.lesserafim.api.dto.LiveTicketDTO;
import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.api.dto.PhotoCardDTO;
import com.lesserafim.api.dto.PhotoCardDTOTest;
import com.lesserafim.api.dto.ZoomDTO;

public interface MemberService
{
	public ApiResponse getUserPhotoCardBitmap(MemberPrincial memberPrincial);

	public ApiResponse insertPhotoCard(String data, MemberPrincial memberPrincial);

	public ApiResponse refreshMember(HttpServletRequest request, HttpServletResponse response);

	public ApiResponse weverseLoginMember(HttpServletRequest request, HttpServletResponse response, String weverseAccessToken);

	public ApiResponse loginTest(HttpServletRequest request, HttpServletResponse response, String weverseAccessToken);

	public ApiResponse insertPhotoCardTest(PhotoCardDTOTest photoCardDTOTest, MemberPrincial memberPrincial);

	public ApiResponse liveTicket(LiveTicketDTO liveTicketDTO, MemberPrincial memberPrincial);

	public ApiResponse getLiveTicket(MemberPrincial memberPrincial);

	public ApiResponse generateSignature(ZoomDTO zoomDTO);

	public ApiResponse getTicketCheck(MemberPrincial memberPrincial);
}
