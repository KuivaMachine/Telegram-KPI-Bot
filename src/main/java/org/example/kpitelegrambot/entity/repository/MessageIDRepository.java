package org.example.kpitelegrambot.entity.repository;

import org.example.kpitelegrambot.entity.MessageID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageIDRepository extends JpaRepository<MessageID, Integer> {
}
