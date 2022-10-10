package com.lesserafim.api.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lesserafim.api.dto.LiveTicketDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LIVETICKET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiveTicket {
	public LiveTicket(LiveTicketDTO liveTicketDTO, Member member) {
		this.birth = liveTicketDTO.getBirth();
		this.memberId = member.getId();
		this.name1 = liveTicketDTO.getName1();
		this.name2 = liveTicketDTO.getName2();
		this.quiz1 = liveTicketDTO.getQuiz1();
		this.quiz2 = liveTicketDTO.getQuiz2();
		this.sex = liveTicketDTO.getSex();
		this.national = "NO";
		this.createdAt = LocalDateTime.now();
	}

	@JsonIgnore
    @Id
    @Column(name = "ticket_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "name1")
    @NotNull
    private String name1;
    
    @Column(name = "name2")
    @NotNull
    private String name2;
    
    @Column(name = "quiz1")
    @NotNull
    private int quiz1;
    
    @Column(name = "quiz2")
    @NotNull
    private int quiz2;
    
    @Column(name = "birth")
    @NotNull
    private String birth;
    
    @Column(name = "sex")
    @NotNull
    private String sex;
    
    @Column(name = "national")
    @NotNull
    private String national;
    
    @Column(name = "CREATED_AT")
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "member_id")
    @NotNull
    private int memberId;
}
