package com.socialsports.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final EventService eventService;

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void processEventReminders() {
        log.info("Running scheduled task: processEventReminders");
        eventService.sendEventReminders();
    }
}