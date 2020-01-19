package com.ubs.spyda.schedulertcpclient;


import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.ubs.spyda.schedulertcpclient.SchedulerTcpServerApplication.jobClientHandlerMapping;

@RestController
@Log4j2
public class TestController {

    @GetMapping("test/{appName}")
    public void test(@PathVariable String appName) throws IOException {
        ClientHandler clientHandler = getRandomClientHandler(appName);
        clientHandler.dos.writeUTF("CONFIRMING MESSAGE FOR " + appName);
        log.debug(String.format("MESSAGE SENT TO %s ON %s", clientHandler.s, clientHandler.getName()));
    }

    @GetMapping("get")
    public Map<String, String> getMap() {
        Map<String, String> stringStringMap = new HashMap<>();
        jobClientHandlerMapping.forEach((s, clientHandlers) -> {
            clientHandlers.forEach(clientHandler -> stringStringMap.put(clientHandler.getName(), s));
        });
        return stringStringMap;
    }

    private ClientHandler getRandomClientHandler(String appName) {
        int length = jobClientHandlerMapping.get(appName).size();
        Random random = new Random();
        List<ClientHandler> clientHandlers = jobClientHandlerMapping.get(appName);
        return clientHandlers.get(random.nextInt(length));
    }
}
