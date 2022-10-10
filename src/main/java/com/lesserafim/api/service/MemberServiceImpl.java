package com.lesserafim.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.redisson.api.RBucket;
import org.redisson.api.RTransaction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesserafim.api.dto.WeverseDTO;
import com.lesserafim.api.dto.ZoomDTO;
import com.lesserafim.api.entity.LiveTicket;
import com.lesserafim.api.entity.Member;
import com.lesserafim.api.entity.PhotoCard;
import com.lesserafim.api.entity.RoleType;
import com.lesserafim.api.dto.LiveDTO;
import com.lesserafim.api.dto.LiveTicketDTO;
import com.lesserafim.api.dto.MemberPrincial;
import com.lesserafim.api.dto.MemberResponseDTO;
import com.lesserafim.api.dto.PhotoCardDTOTest;
import com.lesserafim.api.redis.MyRedis;
import com.lesserafim.api.repository.LiveTicketRepository;
import com.lesserafim.api.repository.MemberRepository;
import com.lesserafim.api.repository.PhotoCardRepository;
import com.lesserafim.common.ApiResponse;
import com.lesserafim.config.properties.AppProperties;
import com.lesserafim.oauth.token.AuthToken;
import com.lesserafim.oauth.token.AuthTokenProvider;
import com.lesserafim.oauth.token.LoginProvider;
import com.lesserafim.utils.CookieUtil;
import com.lesserafim.utils.HeaderUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.security.InvalidKeyException;
import javax.xml.bind.DatatypeConverter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService
{
	private final MemberRepository memberRepository;
	private final PhotoCardRepository photoCardRepository;
	private final LiveTicketRepository liveTicketRepository;
	private final MyRedis myRedis;
	private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final LoginProvider loginProvider;
    private final ObjectMapper objectMapper;
    private final AsyncService asyncService;
    private final DBSelectService dbSelectService;
    private final static long THREE_DAYS_MSEC = 259200000;
    private final static String REFRESH_TOKEN = "ref_uid";
    private final static String ADMIN_TOKEN = "ad_uid";
    @Override
	public ApiResponse weverseLoginMember(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String weverseAccessToken) {
		HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + weverseAccessToken);
        headers.set("X-BENX-CI",loginProvider.getCi());
        headers.set("X-BENX-CIK",loginProvider.getCik());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity request = new HttpEntity(headers);
        String url = loginProvider.getUrl();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            WeverseDTO weverDTO = objectMapper.readValue(response.getBody(), WeverseDTO.class);
            
            String memberKey = weverDTO.getUserKey();
            Member member = dbSelectService.selectMember(memberKey);
            
            Date now = new Date();
            long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();
            AuthToken refreshToken = tokenProvider.createAuthToken(
                    appProperties.getAuth().getTokenSecret(),
                    new Date(now.getTime() + refreshTokenExpiry)
            );
            
    		if(member == null)
    		{
    			member = new Member(
    					memberKey,
    					weverseAccessToken,
    					refreshToken.getToken(),
    				    RoleType.MEMBER,
    					LocalDateTime.now());
    			asyncService.asyncInsertMember(member);
    			myRedis.setValue(memberKey,0);
    		}
    		else
    		{
    			member.setWeverseAccessToken(weverseAccessToken);
    			member.setRefreshToken(refreshToken.getToken());
    			asyncService.asyncUpdateMember(member);
    		}
    		
    		AuthToken accessToken = tokenProvider.createAuthToken(
            		memberKey,
            		weverseAccessToken,
            		member.getRoleType().getCode(),
                    new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
            );
    		
            int cookieMaxAge = (int) refreshTokenExpiry / 1000;
            CookieUtil.deleteCookie(httpRequest, httpResponse, REFRESH_TOKEN, loginProvider.getDomain());
            CookieUtil.addCookie(httpResponse, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge, loginProvider.getDomain());

            MemberResponseDTO memberResponseDTO = new MemberResponseDTO(accessToken.getToken(), false);
        	if(member.getRoleType() == RoleType.ADMIN)
    		{
        		memberResponseDTO.setRole(true);
    		}
        	
            return ApiResponse.success("token",memberResponseDTO);
        }catch (RestClientException ex) {
            ex.printStackTrace();
        } 
        catch (JsonMappingException e) {
			e.printStackTrace();
        } catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return ApiResponse.fail();
	}
    
    @Transactional
	@Override
	public ApiResponse loginTest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String weverseAccessToken) {
    	String memberKey = weverseAccessToken;
        Member member = memberRepository.findByMemberKey(memberKey);
        
        Date now = new Date();
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();
        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );
        
		if(member == null)
		{
			member = new Member(
					memberKey,
					weverseAccessToken,
					refreshToken.getToken(),
				    RoleType.MEMBER,
					LocalDateTime.now());
			asyncService.asyncInsertMember(member);
			myRedis.setValue(memberKey,0);
		}
		else
		{
			member.setWeverseAccessToken(weverseAccessToken);
			member.setRefreshToken(refreshToken.getToken());
		}
		
		AuthToken accessToken = tokenProvider.createAuthToken(
        		memberKey,
        		weverseAccessToken,
        		member.getRoleType().getCode(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );
		
	
        int cookieMaxAge = (int) refreshTokenExpiry / 1000;
        CookieUtil.deleteCookie(httpRequest, httpResponse, REFRESH_TOKEN, loginProvider.getDomain());
        CookieUtil.addCookie(httpResponse, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge, loginProvider.getDomain());
        
        MemberResponseDTO memberResponseDTO = new MemberResponseDTO(accessToken.getToken(), false);
    	if(member.getRoleType() == RoleType.ADMIN)
		{
    		memberResponseDTO.setRole(true);
		}
    	
        return ApiResponse.success("token",memberResponseDTO);
	}
	
	@Override
    public ApiResponse refreshMember(HttpServletRequest request, HttpServletResponse response) {
		// access token 확인
        String accessToken = HeaderUtil.getAccessToken(request);
        
        AuthToken authToken = tokenProvider.convertAuthToken(accessToken);
     
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            return ApiResponse.notExpiredTokenYet();
        }

        String memberKey = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get(AuthToken.AUTHORITIES_KEY, String.class));
        String weverseAccessToken =claims.get(AuthToken.ACCESS_TOKEN, String.class);

        Member member = dbSelectService.selectMember(memberKey);
        
        if(member.getWeverseAccessToken().compareTo(weverseAccessToken) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        // refresh token
        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse((null));
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        if (!authRefreshToken.validate()) {
            return ApiResponse.invalidRefreshToken();
        }

        // userId refresh token 으로 DB 확인
        String memberRefreshToken = member.getRefreshToken();
        if (memberRefreshToken == null) {
            return ApiResponse.invalidRefreshToken();
        }

        Date now = new Date();
        AuthToken newAccessToken = tokenProvider.createAuthToken(
        		memberKey,
        		weverseAccessToken,
                roleType.getCode(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

        // refresh 토큰 기간이 3일 이하로 남은 경우, refresh 토큰 갱신
        if (validTime <= THREE_DAYS_MSEC) {
            // refresh 토큰 설정
            long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

            authRefreshToken = tokenProvider.createAuthToken(
                    appProperties.getAuth().getTokenSecret(),
                    new Date(now.getTime() + refreshTokenExpiry)
            );

            // DB에 refresh 토큰 업데이트
            member.setRefreshToken(authRefreshToken.getToken());
            memberRepository.flush();
            
            int cookieMaxAge = (int) refreshTokenExpiry / 1000;
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN, loginProvider.getDomain());
            CookieUtil.addCookie(response, REFRESH_TOKEN, authRefreshToken.getToken(), cookieMaxAge, loginProvider.getDomain());
        }

        return ApiResponse.success("token", newAccessToken.getToken());
	}
	
	@Override
	public ApiResponse getUserPhotoCardBitmap(MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
	        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        return ApiResponse.success("bitmap", Integer.parseInt((String) myRedis.getValue(memberPrincial.getMemberKey())));
	}

	private boolean checkTime(int girlId) {
		LocalDateTime nowTime = LocalDateTime.now();
		LocalDateTime checkTime = LocalDateTime.of(2022, Month.APRIL, 4+girlId ,13 ,0 ,0);
		if(nowTime.isAfter(checkTime))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public ApiResponse insertPhotoCard(String data,  MemberPrincial memberPrincial) {
		String REACT_APP_K1 = "sfj4fjl59jwljvkcjvklwnvwmd";
		String REACT_APP_K2 = "zxgvbcet3dsf21dfsxvws2345d";
		String REACT_APP_K3 = "xcvw2ed53w231dwfa641dvw35b";
		String REACT_APP_K4 = "zafgcd45jffg2ddxzvrwnfhrez";
		
		if(data.contains(REACT_APP_K1) == false || data.contains(REACT_APP_K2) == false || data.contains(REACT_APP_K3) == false || data.contains(REACT_APP_K4) == false)
		{
			return ApiResponse.success("ret", false);
		}
		
		int at = 26;
		int girlId = 0;
		int imageId = 0;
		int messageId = 0;
		int audioId = 0;
		
		try
		{
			girlId = Integer.parseInt(""+data.charAt(at));
			
			if(checkTime(girlId) == false){
				return ApiResponse.success("ret", false);
			}
			
			if(data.charAt(at+1) != 'z')
			{
				return ApiResponse.success("ret", false);
			}
			
			at+=27;
			
			imageId = Integer.parseInt(""+data.charAt(at));
			if(data.charAt(at+1) != 'x')
			{
				at+=1;
				imageId *= 10;
				imageId += Integer.parseInt(""+data.charAt(at));
			}
			
			if(data.charAt(at+1) != 'x')
			{
				return ApiResponse.success("ret", false);
			}
			
			at+=27;
			
			messageId = Integer.parseInt(""+data.charAt(at));
			if(data.charAt(at+1) != 'z')
			{
				at+=1;
				messageId *= 10;
				messageId += Integer.parseInt(""+data.charAt(at));
			}
			
			if(data.charAt(at+1) != 'z')
			{
				return ApiResponse.success("ret", false);
			}
			
			at+=27;
			
			audioId = Integer.parseInt(""+data.charAt(at));
			
			if(girlId > 5 || imageId > 19 || audioId > 6 || messageId > 11)
			{
				return ApiResponse.success("ret", false);
			}
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			return ApiResponse.success("ret", false);
		}
		
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        /* 유저의 오디오와 메세지 변경하기.
        boolean ret = false;
		int savedBitmap = Integer.parseInt((String) myRedis.getValue(memberPrincial.getMemberKey()));
		int girlBitmap = 1 << girlId;
		
		if((savedBitmap & girlBitmap) != 0)
		{
			List<PhotoCard> list = member.getPhotoCards();
			for(int i = 0; i < list.size();i++)
			{
				if(list.get(i).getGirlId() == girlId && list.get(i).getImageId() == imageId)
				{
					ret = true;
					list.get(i).setAudioId(audioId);
					list.get(i).setMessageId(messageId);
					asyncService.asyncInsertPhotoCard(list.get(i));
				}
			}    		
    	}
		else
		{
			ret = false;
		}
		*/
        
        
		String lock = "lock_key_"+girlId+"_"+imageId;
		boolean ret = false;
		try {
            if (myRedis.tryLock(lock, TimeUnit.SECONDS, 15L, 1L)) {
            	int imageNum = Integer.parseInt((String) myRedis.getValue(""+(girlId*20+imageId)));
        		if(imageNum >= 1000)
        		{
        			ret =  false;
        		}
        		else
        		{
        			int savedBitmap = Integer.parseInt((String) myRedis.getValue(memberPrincial.getMemberKey()));
        			int girlBitmap = 1 << girlId;
        			
        			if((savedBitmap & girlBitmap) == 0)
        			{
        				myRedis.setValue(memberPrincial.getMemberKey(),(savedBitmap + girlBitmap));

        				imageNum++;
        				myRedis.setValue(""+(girlId*20+imageId),imageNum);

            			PhotoCard photoCard = new PhotoCard(
            					girlId,
            					imageId,
            					audioId,
            					messageId,
            					LocalDateTime.now());
            			asyncService.asyncInsertPhotoCard(photoCard);
            			ret = true;
            		}
        			else
        			{
        				log.info("이미 존재하는데 요청을 했다. front에서 처리가 안됬다.");
        				ret = false;
        			}
        		}
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            ret = false;
        } finally {
           if (myRedis.canUnlock(lock)) {
        	   myRedis.unlock(lock);
    	   } 
        }
		
		return ApiResponse.success("ret", ret);
	}
	
	@Override
	public ApiResponse insertPhotoCardTest(PhotoCardDTOTest photoCardDTOTest,  MemberPrincial memberPrincial) {
		int girlId = photoCardDTOTest.getGirlId();
		int imageId = photoCardDTOTest.getImageId();
		int messageId = photoCardDTOTest.getMessageId();
		int audioId = photoCardDTOTest.getAudioId();

		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
        
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
		String lock = "lock_key_"+girlId+"_"+imageId;
		boolean ret = false;
		try {
            if (myRedis.tryLock(lock, TimeUnit.SECONDS, 15L, 1L)) {
            	int imageNum = Integer.parseInt((String) myRedis.getValue(""+(girlId*20+imageId)));
        		if(imageNum >= 1000)
        		{
        			ret =  false;
        		}
        		else
        		{
        			int savedBitmap = Integer.parseInt((String) myRedis.getValue(memberPrincial.getMemberKey()));
        			int girlBitmap = 1 << girlId;
        			
        			if((savedBitmap & girlBitmap) == 0)
        			{
        				myRedis.setValue(memberPrincial.getMemberKey(),(savedBitmap + girlBitmap));

        				imageNum++;
        				myRedis.setValue(""+(girlId*20+imageId),imageNum);

            			PhotoCard photoCard = new PhotoCard(
            					girlId,
            					imageId,
            					audioId,
            					messageId,
            					LocalDateTime.now());
            			asyncService.asyncInsertPhotoCard(photoCard);
            			ret = true;
            		}
        			else
        			{
        				log.info("이미 존재하는데 요청을 했다. front에서 처리가 안됬다.");
        				ret = false;
        			}
        		}
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            ret = false;
        } finally {
           if (myRedis.canUnlock(lock)) {
        	   myRedis.unlock(lock);
    	   } 
        }
		return ApiResponse.success("ret", ret);
	}

	@Override
	public ApiResponse getLiveTicket(MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
	    boolean ret = true;
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        
        String check = (String) myRedis.getValue(memberPrincial.getMemberKey()+"_ticket");
        if(check != null)
        {
        	ret = false;
        }
        
        return ApiResponse.success("ret", ret);
	}
	
	@Transactional
	@Override
	public ApiResponse liveTicket(LiveTicketDTO liveTicketDTO, MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
        boolean ret = true;
        if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
        String check = (String) myRedis.getValue(memberPrincial.getMemberKey()+"_ticket");

        if(liveTicketDTO.getBirth().length() != 8 || liveTicketDTO.getName1().equals("") || liveTicketDTO.getName2().equals("") || liveTicketDTO.getQuiz1() == 0 || liveTicketDTO.getQuiz2() == 0 || liveTicketDTO.getSex().equals(""))
        {
        	ret = false;
        } else if(check != null) {
        	ret = false;
        } else {
            if(member.getPhotoCards().size() != 0)
            {
            	LiveTicket liveticket2 = new LiveTicket(liveTicketDTO, member);
                
                LiveTicket saveLiveTicket2 = liveTicketRepository.saveAndFlush(liveticket2);
            }
            LiveTicket liveticket = new LiveTicket(liveTicketDTO, member);
            
            LiveTicket saveLiveTicket = liveTicketRepository.saveAndFlush(liveticket);
            
            myRedis.setValue(memberPrincial.getMemberKey()+"_ticket","1");
        }
        
		return ApiResponse.success("ret", ret);
	}

	@Override
	public ApiResponse generateSignature(ZoomDTO zoomDTO) {
		int iat = Math.round(((new Date()).getTime() - 30000) / 1000);
		int exp = iat + 60 * 60 * 2;
		Map<String, Object> header = new HashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");
		Map<String, Object> payload = new HashMap<>();
		payload.put("sdkKey", "SvZziQsh4vVJx3IM5gxR4ySGXYf6EKxbsPwv");
		payload.put("mn", "83051137739");
		payload.put("role",zoomDTO.getRole());
		payload.put("iat",iat);
		payload.put("exp",exp);
		payload.put("appKey","SvZziQsh4vVJx3IM5gxR4ySGXYf6EKxbsPwv");
		payload.put("tokenExp",exp);
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		JwtBuilder builder = Jwts.builder()
				.setHeader(header)
				.setPayload(Jackson.toJsonString(payload))
				.signWith(signatureAlgorithm, "AcV66ENBfCFPZrBs7s7FuoOeBE48V0TtQr3Q".getBytes());
				
		LiveDTO liveDTO = new LiveDTO(builder.compact(),"83051137739","586034","SvZziQsh4vVJx3IM5gxR4ySGXYf6EKxbsPwv");
		
		return ApiResponse.success("ret", liveDTO);
	}

	@Override
	public ApiResponse getTicketCheck(MemberPrincial memberPrincial) {
		Member member = dbSelectService.selectMember(memberPrincial.getMemberKey());
		boolean ret = false;
		if(member.getWeverseAccessToken().compareTo(memberPrincial.getAccessToken()) != 0)
        {
        	return ApiResponse.doubleLogin();
        }
		String check = (String) myRedis.getValue(memberPrincial.getMemberKey()+"_winner");
		if(check != null) {
	    	ret = true;
	    } 
		// TODO Auto-generated method stub
		return ApiResponse.success("ret", ret);
	}
}
