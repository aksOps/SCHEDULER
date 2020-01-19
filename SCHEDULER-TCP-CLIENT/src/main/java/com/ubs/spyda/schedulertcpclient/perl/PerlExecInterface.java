package com.ubs.spyda.schedulertcpclient.perl;


import com.ubs.spyda.schedulertcpclient.common.CommonExecInterface;

public interface PerlExecInterface extends CommonExecInterface {

    default boolean runPerl(String s) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String getFunctionToTrigger = this.getValueForString(s, "functionToTrigger");
        processBuilder.command(getFunctionToTrigger.split(" "));
        return this.run(processBuilder, s);
    }
}
