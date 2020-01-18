package com.ubs.spyda.scheduler.controllers;


import com.ubs.spyda.scheduler.constant.JobType;
import com.ubs.spyda.scheduler.constant.TimezoneEnum;
import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.repository.SchedulerRepository;
import com.ubs.spyda.scheduler.service.SchedulerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Log4j2
public class SchedulerController {


    @Autowired
    SchedulerRepository schedulerRepository;

    @Autowired
    SchedulerService schedulerService;

    @GetMapping("getAll")
    public List<SchedulerEntity> getAll() {
        return schedulerRepository.findAll();
    }

    @GetMapping("getById")
    public SchedulerEntity getById(@RequestParam Integer id) {
        Optional<SchedulerEntity> optionalSchedulerEntity = schedulerRepository.findById(id);
        return optionalSchedulerEntity.orElse(null);
    }

    @PostMapping("add")
    public SchedulerEntity add(String minutes, String hour,
                               String dayOfMonth, String month, String dayOfWeek,
                               boolean ifWeekDayEnable,
                               String jobName, JobType jobType, String functionToTrigger,
                               TimezoneEnum timezoneEnum,
                               @RequestParam(required = false, defaultValue = "true") boolean isActive,
                               @RequestParam(required = false, defaultValue = "NONE") String dependentJobIds,
                               String logLocation,
                               @RequestParam(required = false, defaultValue = "yyyyMMddHHmmss") String timeStampPattern) {
        SchedulerEntity schedulerEntity = SchedulerEntity.builder()
                .cronExpression(schedulerService.cronExpression(minutes, hour, dayOfMonth, month, dayOfWeek, ifWeekDayEnable))
                .dependentJobIds(dependentJobIds)
                .functionToTrigger(functionToTrigger)
                .isActive(isActive)
                .jobName(jobName)
                .jobType(jobType)
                .logLocation(logLocation)
                .timeStampPattern(timeStampPattern)
                .timezoneEnum(timezoneEnum)
                .build();
        schedulerRepository.save(schedulerEntity);
        SchedulerEntity entity = schedulerRepository.findByJobName(schedulerEntity.getJobName());
        if (entity.isActive()) {
            schedulerService.addTaskToScheduler(entity);
        }
        return schedulerRepository.getOne(entity.getId());
    }

    @PostMapping("refresh")
    public void refresh() {
        schedulerService.loadScheduler();
    }

    @PutMapping("update")
    public SchedulerEntity update(@RequestBody SchedulerEntity schedulerEntity) {
        schedulerRepository.save(schedulerEntity);
        SchedulerEntity entity = schedulerRepository.findByJobName(schedulerEntity.getJobName());
        if (entity.isActive()) {
            schedulerService.addTaskToScheduler(entity);
        } else {
            schedulerService.removeTaskFromScheduler(entity.getId());
        }
        return schedulerRepository.getOne(entity.getId());
    }

    @DeleteMapping("remove")
    public void remove(@RequestParam Integer id) {
        schedulerService.removeTaskFromScheduler(id);
        schedulerRepository.deleteById(id);
    }


}
