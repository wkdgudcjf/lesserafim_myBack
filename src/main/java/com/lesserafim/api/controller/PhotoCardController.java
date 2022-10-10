package com.lesserafim.api.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lesserafim.api.dto.ImageDTO;
import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.api.service.PhotoCardService;
import com.lesserafim.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photocard")
public class PhotoCardController {
	
	private final PhotoCardService photoCardService;
    
	@RequestMapping(value = "/photoCardValidation", method = {RequestMethod.GET})
	public ApiResponse photoCardValidation(@RequestParam("girl_id") int girlId){
		return photoCardService.getPhotoCardValidation(girlId);
	}
	
	@RequestMapping(value = "/userPhotoCardList", method = {RequestMethod.GET})
	public ApiResponse userPhotoCardList(MemberPrincial memberPrincial){
		return photoCardService.getUserPhotoCardList(memberPrincial);
	}
	
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/adminPhotoCardList", method = {RequestMethod.GET})
	public ApiResponse adminPhotoCardList(MemberPrincial memberPrincial){
		return photoCardService.getAdminPhotoCardList(memberPrincial);
	}
	
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/adminPhotoCard", method = RequestMethod.POST, consumes = {"multipart/form-data"})
	public ApiResponse adminPhotoCard(@ModelAttribute ImageDTO imageDTO, MemberPrincial memberPrincial){
		return photoCardService.insertAdminPhotoCard(imageDTO.getImageFront(), imageDTO.getImageBack(), imageDTO.getMId(), memberPrincial);
	}
	
    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/deleteAdminPhotoCard", method = {RequestMethod.GET})
    public ApiResponse deletePhotoCard(@RequestParam("mId") int mId, MemberPrincial memberPrincial){
        return photoCardService.deleteKey(mId, memberPrincial);
    }
}
