package com.learnly.repository;

import com.learnly.entity.AiMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByUserIdOrderByCreatedAtAsc(Long userId);
    List<AiMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
