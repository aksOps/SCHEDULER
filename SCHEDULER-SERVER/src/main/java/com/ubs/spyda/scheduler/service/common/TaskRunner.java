package com.ubs.spyda.scheduler.service.common;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.service.java.JavaExecInterface;
import com.ubs.spyda.scheduler.service.perl.PerlExecInterface;
import com.ubs.spyda.scheduler.service.shell.ShellExecInterface;

public interface TaskRunner extends JavaExecInterface, ShellExecInterface, PerlExecInterface {

    default boolean exec(SchedulerEntity schedulerEntity) {
        switch (schedulerEntity.getJobType()) {
            case LINUX_COMMAND:
                return this.runLinuxCommand(schedulerEntity);
            case LINUX_SCRIPT:
                return this.runLinuxScript(schedulerEntity);
            case WIN_COMMAND:
                return this.runWinCommand(schedulerEntity);
            case WIN_SCRIPT:
                return this.runWinScript(schedulerEntity);
            case PERL:
                return this.runPerl(schedulerEntity);
            case JAVA:
                return this.runJava(schedulerEntity);
            default:
                return false;
        }
    }
}
