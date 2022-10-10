package com.lesserafim.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.api.dto.UserPhotoCard;
import com.lesserafim.api.entity.Member;
import com.lesserafim.api.redis.MyRedis;
import com.lesserafim.api.repository.MemberRepository;
import com.lesserafim.common.ApiResponse;
import com.lesserafim.config.s3.S3Manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoCardServiceImpl implements PhotoCardService
{
	private final MemberRepository memberRepository;
	private final MyRedis myRedis;
	private final DBSelectService dbSelectService;
	private final S3Manager s3Manager;
	@Override
	public ApiResponse getUserPhotoCardList(MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
	        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        List<UserPhotoCard> list = new ArrayList<UserPhotoCard>();
        
        for(int i = 0; i < member.getPhotoCards().size(); i++)
        {
        	UserPhotoCard upc = new UserPhotoCard();
        	upc.setGirlId(member.getPhotoCards().get(i).getGirlId());
        	upc.setImageId(member.getPhotoCards().get(i).getImageId());
        	upc.setAudioId(member.getPhotoCards().get(i).getAudioId());
        	upc.setMessageId(member.getPhotoCards().get(i).getMessageId());
        	upc.setFrontUrl((String) myRedis.getValue("image_front_"+member.getPhotoCards().get(i).getGirlId()));
        	upc.setBackUrl((String) myRedis.getValue("image_back_"+member.getPhotoCards().get(i).getGirlId()));
        	list.add(upc);
        }
        
		return ApiResponse.success("list",list);
	}
	
	@Override
	public ApiResponse getAdminPhotoCardList(MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
	        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        String imageList[][] = new String[2][6];
        
        for(int i = 0; i < 6; i++)
        {
        	imageList[0][i] =  (String) myRedis.getValue("image_front_"+i);
        	imageList[1][i] =  (String) myRedis.getValue("image_back_"+i);
        }
        
		return ApiResponse.success("imageList",imageList);
	}

	@Override
	public ApiResponse getPhotoCardValidation(int girlId) {
        int imageValidList[] = new int[20];
        for(int i = 0; i<20; i++)
        {
        	imageValidList[i] =  Integer.parseInt((String) myRedis.getValue(""+(girlId*20+i)));
        }
		return ApiResponse.success("imageValidList",imageValidList);
	}
	
	static String randomWord(String selectCase, int length){
        if (selectCase == "lower"){
            String lowerRandom ="";
            for (int i = 0; i < length; i++) {
                char lowerCh = (char)((int)(Math.random()*25) + 97);
                lowerRandom += lowerCh;
            }
            return lowerRandom;
        }
        if (selectCase == "upper"){
            String upperRandom ="";
            for (int i = 0; i < length; i++) {
                char upperCh = (char)((int)(Math.random()*25) + 65);
                upperRandom += upperCh;
            }
            return upperRandom;
        }
        if (selectCase == "number"){
            String numRandom ="";
            for (int i = 0; i < length; i++) {
                char ch = (char)((int)(Math.random()*10) + 48);
                numRandom += ch;
            }
            return numRandom;
        }
        return null;
    }

	@Override
	public ApiResponse insertAdminPhotoCard(MultipartFile imageFront, MultipartFile imageBack, int girl_id,	MemberPrincial memberPrincial) {
		String dirName = "special";
		
		String contentTypeFront = imageFront.getContentType();
        String originalFileExtensionFront = null;
        
        if(contentTypeFront.contains("image/jpeg")){
        	originalFileExtensionFront = ".jpg";
        }
        else if(contentTypeFront.contains("image/png")){
        	originalFileExtensionFront = ".png";
        }
        else if(contentTypeFront.contains("video/mp4")){
        	originalFileExtensionFront = ".mp4";
        }
        else {
        	return ApiResponse.fail();
        }
        
        String contentTypeBack = imageBack.getContentType();
        String originalFileExtensionBack = null;
        
        if(contentTypeBack.contains("image/jpeg")){
        	originalFileExtensionBack = ".jpg";
        }
        else if(contentTypeBack.contains("image/png")){
        	originalFileExtensionBack = ".png";
        }
        else if(contentTypeBack.contains("video/mp4")){
        	originalFileExtensionBack = ".mp4";
        }
        else {
        	return ApiResponse.fail();
        }
        
        Random random = new Random();
        int length = random.nextInt(5)+20;
 
        StringBuffer newWord = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int mixed = random.nextInt(3);
            switch(mixed) {
                case 0:
                    newWord.append(randomWord("lower", 1));
                    break;
                case 1:
                    newWord.append(randomWord("upper", 1));
                    break;
                case 2:
                    newWord.append(randomWord("number", 1));
                    break;
                default:
                    break;
            }
        }
      
        String imageName = newWord.toString()+originalFileExtensionFront;
        String fullName = dirName+"/"+imageName;
        s3Manager.upload(imageFront, fullName);
        myRedis.setValue("image_front_"+girl_id, fullName);
        
        length = random.nextInt(5)+20;
 
        newWord = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int mixed = random.nextInt(3);
            switch(mixed) {
                case 0:
                    newWord.append(randomWord("lower", 1));
                    break;
                case 1:
                    newWord.append(randomWord("upper", 1));
                    break;
                case 2:
                    newWord.append(randomWord("number", 1));
                    break;
                default:
                    break;
            }
        }
      
        imageName = newWord.toString()+originalFileExtensionBack;
        fullName = dirName+"/"+imageName;
        s3Manager.upload(imageBack, fullName);
        myRedis.setValue("image_back_"+girl_id, fullName);
        
        return ApiResponse.success("uploadImageUrl",fullName);
	}

    @Override
    public ApiResponse deleteKey(int girlId, MemberPrincial memberPrincial) {
        Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
            return ApiResponse.doubleLogin();
        }
        boolean ret = myRedis.deleteKey("image_front_"+girlId);
        ret = myRedis.deleteKey("image_back_"+girlId);
        return ApiResponse.success("ret",ret);
    }
}
