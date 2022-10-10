package com.lesserafim.api.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PHOTOCARD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCard {
    @JsonIgnore
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "GIRL_ID")
    @NotNull
    private int girlId;
    
    @Column(name = "IMAGE_ID")
    @NotNull
    private int imageId;
    
    @Column(name = "AUDIO_ID")
    @NotNull
    private int audioId;
    
    @Column(name = "MESSAGE_ID")
    @NotNull
    private int messageId;
    
    @JsonIgnore
    @Column(name = "CREATED_AT")
    @NotNull
    private LocalDateTime createdAt;
    
    @JsonIgnore
    @ManyToOne
    @NotNull
    private Member member;
    
    public PhotoCard(
            @NotNull int girlId,
            @NotNull int imageId,
            @NotNull int audioId,
            @NotNull int messageId,
            @NotNull LocalDateTime createdAt
    ) {
        this.girlId = girlId;
        this.imageId = imageId;
        this.audioId = audioId;
        this.messageId = messageId;
        this.createdAt = createdAt;
    }
}