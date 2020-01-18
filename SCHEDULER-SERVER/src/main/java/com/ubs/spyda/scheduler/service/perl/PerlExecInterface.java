package com.ubs.spyda.scheduler.service.perl;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.service.common.CommonExecInterface;

public interface PerlExecInterface extends CommonExecInterface {

    default boolean runPerl(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(schedulerEntity.getFunctionToTrigger().split(" "));
        return this.run(processBuilder, schedulerEntity);
    }
}
