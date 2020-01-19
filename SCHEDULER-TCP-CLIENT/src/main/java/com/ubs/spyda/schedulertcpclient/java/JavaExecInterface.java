package com.ubs.spyda.schedulertcpclient.java;


import com.ubs.spyda.schedulertcpclient.common.CommonExecInterface;

public interface JavaExecInterface extends CommonExecInterface {

    default boolean runJava(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command(getFunctionToTrigger.split(" "));
        return this.run(processBuilder, s);
    }
}
