package com.ubs.spyda.schedulertcpclient.shell;


import com.ubs.spyda.schedulertcpclient.common.CommonExecInterface;

public interface ShellExecInterface extends CommonExecInterface {

    default boolean runLinuxScript(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command("bash", "-c", getFunctionToTrigger);
        return this.run(processBuilder, s);
    }

    default boolean runLinuxCommand(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command(getFunctionToTrigger.split(" "));
        return this.run(processBuilder, s);
    }

    default boolean runWinCommand(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command("cmd.exe", "/c", getFunctionToTrigger);
        return this.run(processBuilder, s);
    }

    default boolean runWinScript(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command(getFunctionToTrigger.split(" "));
        return this.run(processBuilder, s);
    }

}
