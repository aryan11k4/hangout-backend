package com.hangout.backend.broadcast.service;

import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.common.enums.BroadcastStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Runs once, on every application startup (ApplicationReadyEvent - fires
 * after the app is fully up, not during bean construction). Ends any
 * broadcast still ACTIVE more than 5 hours after it was created.
 * <p>
 * This exists because BroadcastDisconnectListener no longer auto-ends a
 * broadcast when its host disconnects (per product decision - see that
 * class). Without SOME cleanup mechanism, a host who closes their tab and
 * never comes back would leave a broadcast permanently ACTIVE, since
 * ending one is host-only (POST /broadcasts/{id}/end returns 403 for
 * anyone else) and it would clutter every group's active-broadcasts list
 * forever. A 5-hour threshold is well past any realistic watch session, so
 * this only catches genuinely abandoned broadcasts, not active ones.
 * <p>
 * This is a STARTUP sweep only, not a recurring scheduled job - per the
 * product decision as given ("whenever server starts, it checks..."). A
 * broadcast that goes stale between deploys/restarts will sit stale until
 * the next restart. If you want this to also run periodically while the
 * server stays up (not just at startup), that's a different mechanism
 * (@Scheduled) - say so if you want that added too.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StaleBroadcastCleanupTask {

    private static final long STALE_THRESHOLD_HOURS = 5;

    private final BroadcastRepository broadcastRepository;
    private final BroadcastService broadcastService;

    @EventListener(ApplicationReadyEvent.class)
    public void cleanUpStaleBroadcasts() {
        Instant cutoff = Instant.now().minus(STALE_THRESHOLD_HOURS, ChronoUnit.HOURS);

        List<Broadcast> staleBroadcasts =
                broadcastRepository.findByStatusAndCreatedAtBefore(BroadcastStatus.ACTIVE, cutoff);

        if (staleBroadcasts.isEmpty()) {
            return;
        }

        log.info("Startup cleanup: ending {} stale broadcast(s) active for more than {} hours",
                staleBroadcasts.size(), STALE_THRESHOLD_HOURS);

        for (Broadcast broadcast : staleBroadcasts) {
            broadcastService.endBroadcastInternal(broadcast, "STALE_CLEANUP");
        }
    }
}