package org.example.googlesheetservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduleManager.class);

    @Scheduled(cron = "1 * * * * *")
    public void doWork() {
        log.info("doWork");
    }
}
