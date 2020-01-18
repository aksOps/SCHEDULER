package com.ubs.spyda.scheduler.service;


import com.ubs.spyda.scheduler.constant.StatusEnum;
import com.ubs.spyda.scheduler.constant.TimezoneEnum;
import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.repository.SchedulerRepository;
import com.ubs.spyda.scheduler.service.common.TaskRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Service
@Log4j2
public class SchedulerService implements TaskRunner {

    public static volatile Map<Integer, StatusEnum> jobStatusMap = new HashMap<>();

    @Autowired
    TaskScheduler taskScheduler;

    @Autowired
    SchedulerRepository schedulerRepository;

    private Map<Integer, ScheduledFuture<?>> jobsMap = new HashMap<>();

    private CronTrigger cronTrigger(String cronExpression, TimezoneEnum timezoneEnum) {
        return new CronTrigger(cronExpression, TimeZone.getTimeZone(timezoneEnum.name()));
    }

    public void addTaskToScheduler(SchedulerEntity schedulerEntity) {
        removeTaskFromScheduler(schedulerEntity.getId());
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(runnable(schedulerEntity), cronTrigger(schedulerEntity.getCronExpression(), schedulerEntity.getTimezoneEnum()));
        jobsMap.put(schedulerEntity.getId(), scheduledTask);
    }

    public void removeTaskFromScheduler(int id) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(id);
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            jobsMap.put(id, null);
        }
    }

    private Runnable runnable(SchedulerEntity schedulerEntity) {
        return () -> {
            if (!schedulerEntity.getDependentJobIds().equals("NONE")) {
                waitForDependency(schedulerEntity);
            } else {
                run(schedulerEntity);
            }
        };
    }

    private void run(SchedulerEntity schedulerEntity) {
        Runnable runnable = () -> executeTrigger(schedulerEntity);
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public void loadScheduler() {
        List<SchedulerEntity> schedulerEntities = schedulerRepository.findAll();
        schedulerEntities.stream().filter(SchedulerEntity::isActive).forEach(this::addTaskToScheduler);
        log.info("##################################################");
        log.info(String.format("### Total Schedules : %d", schedulerEntities.size()));
        log.info(String.format("### Total Active Schedule : %d", schedulerEntities.stream().filter(SchedulerEntity::isActive).count()));
        log.info(String.format("### Total In-Active Schedule : %d", schedulerEntities.stream().filter(schedulerEntity -> !schedulerEntity.isActive()).count()));
        log.info("##################################################");
    }

    private void waitForDependency(SchedulerEntity schedulerEntity) {
        Runnable runnable = () -> {
            List<SchedulerEntity> schedulerEntities = schedulerRepository.findAll().stream().filter(SchedulerEntity::isActive).collect(Collectors.toList());
            for (String i : schedulerEntity.getDependentJobIds().split(",")) {
                Integer id = Integer.valueOf(i);
                Optional<SchedulerEntity> optionalSchedulerEntity = schedulerEntities.stream().filter(entity -> entity.getId().equals(id)).findFirst();
                if (optionalSchedulerEntity.isPresent()) {
                    ScheduledFuture<?> scheduledTask = jobsMap.get(id);
                    if (scheduledTask != null) {
                        while (jobStatusMap.get(id) == StatusEnum.RUNNING || jobStatusMap.get(id) == StatusEnum.STALE) {
                            log.info(String.format("%s : waiting for %s to complete", schedulerEntity.getJobName(), optionalSchedulerEntity.get().getJobName()));
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (jobStatusMap.get(id) == StatusEnum.FAILED) {
                            addTaskToScheduler(schedulerEntity);
                        }
                    }
                }
            }
            run(schedulerEntity);
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    private void executeTrigger(SchedulerEntity schedulerEntity) {
        jobStatusMap.put(schedulerEntity.getId(), StatusEnum.RUNNING);
        try {
            // <editor-fold defaultstate="collapsed" desc="Function to be implemented">
            log.info(String.format("Triggering %s", schedulerEntity.getJobName()));
            if (this.exec(schedulerEntity)) {
                jobStatusMap.put(schedulerEntity.getId(), StatusEnum.COMPLETED);
                log.info(String.format("Successfully completed %s ", schedulerEntity.getJobName()));
            } else {
                jobStatusMap.put(schedulerEntity.getId(), StatusEnum.FAILED);
                log.error(String.format("Failed to completed %s", schedulerEntity.getJobName()));
            }
            // </editor-fold>
        } catch (Exception e) {
            jobStatusMap.put(schedulerEntity.getId(), StatusEnum.FAILED);
        }


    }

    public String cronExpression(String minutes, String hour, String dayOfMonth, String month, String dayOfWeek, boolean ifWeekDayEnable) {
        if (validateCronData(minutes, hour, dayOfMonth, month, dayOfWeek)) {
            String cronTemplate = "0 MINUTE HOUR DAYOFMONTH MONTH DAYOFWEEK";
            if (ifWeekDayEnable) {
                cronTemplate = "0 MINUTE HOUR ? MONTH DAYOFWEEK";
            } else {
                cronTemplate = "0 MINUTE HOUR DAYOFMONTH MONTH ?";
            }
            cronTemplate = cronTemplate.replace("MINUTE", unique(minutes))
                    .replace("HOUR", unique(hour))
                    .replace("DAYOFMONTH", unique(dayOfMonth))
                    .replace("MONTH", unique(month))
                    .replace("DAYOFWEEK", unique(dayOfWeek));
            return cronTemplate;
        }
        throw new NullPointerException("Will return Null Pointer, Invalid data provided for cron");
    }

    private boolean validateCronData(String minutes, String hour, String dayOfMonth, String month, String dayOfWeek) {
        return (validateData(minutes, 59, 0)
                && validateData(hour, 23, 0)
                && validateData(dayOfMonth, 31, 1)
                && validateData(month, 12, 1)
                && validateData(dayOfWeek, 7, 1));

    }

    private boolean validateData(String intArray, int maxValue, int minValue) {
        for (String i : intArray.split(",")) {
            Integer integer = Integer.valueOf(i);
            if (integer < minValue || integer > maxValue) {
                return false;
            }
        }
        return true;
    }

    private String unique(String stringWithCommaDelimiter) {
        String[] array = stringWithCommaDelimiter.split(",");
        String[] unique = Arrays.stream(array).distinct().toArray(String[]::new);
        StringBuilder stringBuilder = new StringBuilder();
        for (String i : unique) {
            stringBuilder.append(i).append(" ");
        }
        return stringBuilder.toString().trim().replace(" ", ",");
    }
}
