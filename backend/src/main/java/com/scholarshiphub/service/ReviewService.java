package com.scholarshiphub.service;

import com.scholarshiphub.dto.request.ReviewRequest;
import com.scholarshiphub.dto.response.ReviewResponse;

public interface ReviewService {

    /** Only the reviewer assigned to the application (status UNDER_REVIEW) may review it. */
    ReviewResponse addReview(Long applicationId, Long reviewerId, ReviewRequest request);
}
