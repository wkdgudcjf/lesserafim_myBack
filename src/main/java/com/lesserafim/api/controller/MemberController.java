package com.lesserafim.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lesserafim.api.dto.LiveTicketDTO;
import com.lesserafim.api.dto.LoginDTO;
import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.api.dto.PhotoCardDTO;
import com.lesserafim.api.dto.PhotoCardDTOTest;
import com.lesserafim.api.dto.ZoomDTO;
import com.lesserafim.api.service.MemberService;
import com.lesserafim.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
	
	private final MemberService memberService;
    
	@RequestMapping(value = "/weverseLogin", method = {RequestMethod.POST})
	public ApiResponse weverseLoginMember(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDTO loginDTO){
		return memberService.weverseLoginMember(request, response, loginDTO.getAccessToken());
	}
	
	@RequestMapping(value = "/generateSignature", method = {RequestMethod.POST})
	public ApiResponse generateSignature(@RequestBody ZoomDTO zoomDTO){
		return memberService.generateSignature(zoomDTO);
	}
	
	@RequestMapping(value = "/refresh", method = {RequestMethod.GET})
	public ApiResponse refreshMember(HttpServletRequest request, HttpServletResponse response, MemberPrincial memberPrincial){
		return memberService.refreshMember(request, response);
	}
	
	@RequestMapping(value = "/userPhotoCardBitmap", method = {RequestMethod.GET})
	public ApiResponse userPhotoCardBitmap(MemberPrincial memberPrincial){
		return memberService.getUserPhotoCardBitmap(memberPrincial);
	}
	
	@RequestMapping(value = "/userPhotoCard", method = {RequestMethod.POST})
	public ApiResponse userPhotoCard(@RequestBody PhotoCardDTO photoCardDTO, MemberPrincial memberPrincial){
		return memberService.insertPhotoCard(photoCardDTO.getData(), memberPrincial);
	}
	
	@RequestMapping(value = "/liveTicket", method = {RequestMethod.POST})
	public ApiResponse liveTicket(@RequestBody LiveTicketDTO liveTicketDTO, MemberPrincial memberPrincial){
		return memberService.liveTicket(liveTicketDTO, memberPrincial);
	}
	
	@RequestMapping(value = "/getLiveTicket", method = {RequestMethod.GET})
	public ApiResponse getLiveTicket(MemberPrincial memberPrincial){
		return memberService.getLiveTicket(memberPrincial);
	}
	
	@RequestMapping(value = "/getTicketCheck", method = {RequestMethod.GET})
	public ApiResponse getTicketCheck(MemberPrincial memberPrincial){
		return memberService.getTicketCheck(memberPrincial);
	}
	
	//test
	@RequestMapping(value = "/loginTest", method = {RequestMethod.POST})
	public ApiResponse lgoinTest(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDTO loginDTO){
		return memberService.loginTest(request, response, loginDTO.getAccessToken());
	}
	
	//test
	@RequestMapping(value = "/userPhotoCardTest", method = {RequestMethod.POST})
	public ApiResponse userPhotoCardTest(@RequestBody PhotoCardDTOTest photoCardDTOTest, MemberPrincial memberPrincial){
		return memberService.insertPhotoCardTest(photoCardDTOTest, memberPrincial);
	}
}
