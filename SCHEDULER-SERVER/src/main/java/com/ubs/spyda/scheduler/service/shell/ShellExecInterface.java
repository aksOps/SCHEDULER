package com.ubs.spyda.scheduler.service.shell;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.service.common.CommonExecInterface;

public interface ShellExecInterface extends CommonExecInterface {

    default boolean runLinuxScript(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", schedulerEntity.getFunctionToTrigger());
        return this.run(processBuilder, schedulerEntity);
    }

    default boolean runLinuxCommand(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(schedulerEntity.getFunctionToTrigger().split(" "));
        return this.run(processBuilder, schedulerEntity);
    }

    default boolean runWinCommand(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", schedulerEntity.getFunctionToTrigger());
        return this.run(processBuilder, schedulerEntity);
    }

    default boolean runWinScript(SchedulerEntity schedulerEntity) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(schedulerEntity.getFunctionToTrigger().split(" "));
        return this.run(processBuilder, schedulerEntity);
    }

}
