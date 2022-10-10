package com.lesserafim.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lesserafim.api.entity.LiveTicket;

@Repository
public interface LiveTicketRepository extends JpaRepository<LiveTicket, Integer> {

}