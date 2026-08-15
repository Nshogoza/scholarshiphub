package com.scholarshiphub.service.impl;

import com.scholarshiphub.repository.RefreshTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Periodically purges long-expired refresh tokens so the table doesn't
 *  grow unbounded; expired tokens are already rejected on use, so this is
 *  housekeeping, not a security control. */
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupTask.class);

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // daily at 03:00 server time
    @Transactional
    public void purgeExpiredRefreshTokens() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = refreshTokenRepository.deleteAllExpiredBefore(cutoff);
        if (deleted > 0) {
            log.info("Purged {} expired refresh tokens older than {}", deleted, cutoff);
        }
    }
}
