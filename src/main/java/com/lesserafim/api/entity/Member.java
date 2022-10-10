package com.lesserafim.api.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lesserafim.api.dto.MemberDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MEMBER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {  
    @JsonIgnore
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotNull
    @Column(name = "MEMBER_KEY", length = 256)
    @Size(max = 256)
    private String memberKey;
    
    @Column(name = "WEVERSE_ACCESS_TOKEN", length = 256)
    @NotNull
    @Size(max = 256)
    private String weverseAccessToken;
    
    @Column(name = "ROLE_TYPE")
    @NotNull
    private RoleType roleType;
    
    @Column(name = "REFRESH_TOKEN", length = 256)
    @NotNull
    @Size(max = 256)
    private String refreshToken;
    
    @Column(name = "CREATED_AT")
    @NotNull
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "member")
    private List<PhotoCard> photoCards = new ArrayList<>();
        	
    public Member(
            @NotNull @Size(max = 256) String memberKey,
            @NotNull @Size(max = 256) String weverseAccessToken,
            @NotNull @Size(max = 256) String refreshToken,
            @NotNull RoleType roleType,
            @NotNull LocalDateTime createdAt
    ) {
        this.memberKey = memberKey;
        this.weverseAccessToken = weverseAccessToken;
        this.refreshToken = refreshToken;
        this.roleType = roleType;
        this.createdAt = createdAt;
    }
        
	public MemberDTO toDTO() {
		return new MemberDTO(memberKey,roleType.getCode());
	}
}