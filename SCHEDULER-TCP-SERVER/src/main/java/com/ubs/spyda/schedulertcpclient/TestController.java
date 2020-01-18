package com.ubs.spyda.schedulertcpclient;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.ubs.spyda.schedulertcpclient.SchedulerTcpServerApplication.jobClientHandlerMapping;

@RestController
public class TestController {


    @GetMapping("test/{appName}")
    public void test(@PathVariable String appName) throws IOException {
        jobClientHandlerMapping.get(appName).dos.writeUTF("CONFIRMING MESSAGE FOR " + appName);
    }
}
