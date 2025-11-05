package app.nail.infra.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** English: Periodic job placeholder to rebuild pending counters. */
@Component
public class RecalculatePendingJob {
    // @Scheduled(fixedDelay = 60000)
    public void tick(){
        // English: call RebuildSnapshotService here.
    }
}
