package com.ubs.spyda.scheduler.service.java;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.service.common.CommonExecInterface;

public interface JavaExecInterface extends CommonExecInterface {

    default boolean runJava(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(schedulerEntity.getFunctionToTrigger().split(" "));
        return this.run(processBuilder, schedulerEntity);
    }

}
