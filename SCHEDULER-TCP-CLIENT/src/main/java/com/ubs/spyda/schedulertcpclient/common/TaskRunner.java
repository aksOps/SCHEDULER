package com.ubs.spyda.schedulertcpclient.common;

import com.ubs.spyda.schedulertcpclient.java.JavaExecInterface;
import com.ubs.spyda.schedulertcpclient.perl.PerlExecInterface;
import com.ubs.spyda.schedulertcpclient.shell.ShellExecInterface;

public interface TaskRunner extends JavaExecInterface, PerlExecInterface, ShellExecInterface {

    default boolean exec(String s) {
        String getJobType = this.getValueForString(s, "jobType");
        switch (getJobType) {
            case "LINUX_COMMAND":
                return this.runLinuxCommand(s);
            case "LINUX_SCRIPT":
                return this.runLinuxScript(s);
            case "WIN_COMMAND":
                return this.runWinCommand(s);
            case "WIN_SCRIPT":
                return this.runWinScript(s);
            case "PERL":
                return this.runPerl(s);
            case "JAVA":
                return this.runJava(s);
            default:
                return false;

        }
    }
}
