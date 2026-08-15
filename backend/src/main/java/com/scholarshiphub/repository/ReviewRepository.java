package com.scholarshiphub.repository;

import com.scholarshiphub.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByApplication_IdOrderByCreatedAtAsc(Long applicationId);
}
